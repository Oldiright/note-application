package com.example.noteapplication;

import com.example.noteapplication.dto.NoteCreateRequest;
import com.example.noteapplication.dto.NoteDetailResponse;
import com.example.noteapplication.dto.NoteUpdateRequest;
import com.example.noteapplication.model.Note;
import com.example.noteapplication.model.Tag;
import com.example.noteapplication.repository.NoteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class NoteIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(NoteIntegrationTest.class);

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        log.info("Testcontainers MongoDB URI: {}", mongoDBContainer.getReplicaSetUrl());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoteRepository noteRepository;

    @BeforeEach
    void setUp() {
        noteRepository.deleteAll();
    }

    // Helper methods
    private Note createAndSaveNote(String title, String text, Set<Tag> tags, LocalDateTime createdDate) {
        return noteRepository.save(Note.builder()
                .title(title)
                .text(text)
                .tags(tags != null ? tags : Set.of())
                .createdDate(createdDate)
                .build());
    }

    @Test
    @DisplayName("Should create note with all fields")
    void shouldCreateNoteSuccessfully() throws Exception {
        NoteCreateRequest request = new NoteCreateRequest(
                "Project Planning Meeting",
                "Discuss project milestones, assign tasks to team members, and set deadlines for Q1 deliverables. " +
                        "Review budget allocation and resource requirements.",
                Set.of(Tag.BUSINESS, Tag.IMPORTANT)
        );

        MvcResult result = mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Project Planning Meeting"))
                .andExpect(jsonPath("$.text").value(containsString("project milestones")))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.createdDate").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        NoteDetailResponse response = objectMapper.readValue(responseBody, NoteDetailResponse.class);

        Assertions.assertNotNull(response.id());
        Assertions.assertEquals(1, noteRepository.count());
    }

    @Test
    @DisplayName("Should create note without tags")
    void shouldCreateNoteWithoutTags() throws Exception {
        NoteCreateRequest request = new NoteCreateRequest(
                "Grocery Shopping List",
                "Buy milk, eggs, bread, cheese, and fresh vegetables from the local market.",
                null
        );

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Grocery Shopping List"))
                .andExpect(jsonPath("$.tags").isEmpty());

        Assertions.assertEquals(1, noteRepository.count());
    }

    @Test
    @DisplayName("Should handle note with large text content")
    void shouldHandleNoteWithLargeText() throws Exception {
        String largeText = "word " .repeat(10000);
        NoteCreateRequest request = new NoteCreateRequest(
                "Large Note",
                largeText,
                Set.of(Tag.PERSONAL)
        );

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Large Note"));

        Assertions.assertEquals(1, noteRepository.count());
    }

    // Validation tests
    @Test
    @DisplayName("Should fail to create note without title")
    void shouldFailToCreateNoteWithoutTitle() throws Exception {
        NoteCreateRequest request = new NoteCreateRequest(
                null,
                "This note has no title and should fail validation.",
                Set.of(Tag.PERSONAL)
        );

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("Title is required"));
    }

    @Test
    @DisplayName("Should fail to create note without text")
    void shouldFailToCreateNoteWithoutText() throws Exception {
        NoteCreateRequest request = new NoteCreateRequest(
                "Note with no text",
                null,
                Set.of(Tag.PERSONAL)
        );

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.text").value("Text is required"));
    }

    @Test
    @DisplayName("Should fail with invalid tag")
    void shouldFailWithInvalidTag() throws Exception {
        String invalidRequest = """
                {
                    "title": "Test Note",
                    "text": "Some text",
                    "tags": ["INVALID_TAG"]
                }
                """;

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("INVALID_TAG")))
                .andExpect(jsonPath("$.message").value(containsString("BUSINESS, PERSONAL, IMPORTANT")));
    }

    // List and filter tests
    @Test
    @DisplayName("Should list all notes with pagination")
    void shouldListNotesWithPagination() throws Exception {
        noteRepository.saveAll(
                IntStream.rangeClosed(1, 5)
                        .mapToObj(i -> Note.builder()
                                .title("Note " + i)
                                .text("Content of note number " + i)
                                .tags(Set.of(Tag.PERSONAL))
                                .createdDate(LocalDateTime.now().plusMinutes(i))
                                .build())
                        .toList()
        );

        mockMvc.perform(get("/api/v1/notes?page=0&size=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].title").exists())
                .andExpect(jsonPath("$.content[0].createdDate").exists())
                .andExpect(jsonPath("$.content[0].text").doesNotExist());
    }

    @Test
    @DisplayName("Should list notes sorted by date descending (newest first)")
    void shouldListNotesSortedByDateDesc() throws Exception {
        createAndSaveNote("Old Note", "Created 3 days ago", Set.of(), LocalDateTime.now().minusDays(3));
        createAndSaveNote("Middle Note", "Created 2 days ago", Set.of(), LocalDateTime.now().minusDays(2));
        createAndSaveNote("New Note", "Created today", Set.of(), LocalDateTime.now());

        mockMvc.perform(get("/api/v1/notes?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("New Note"))
                .andExpect(jsonPath("$.content[1].title").value("Middle Note"))
                .andExpect(jsonPath("$.content[2].title").value("Old Note"));
    }

    @Test
    @DisplayName("Should filter notes by tag")
    void shouldFilterNotesByTag() throws Exception {
        createAndSaveNote("Business Meeting", "Quarterly review meeting", Set.of(Tag.BUSINESS));
        createAndSaveNote("Personal Reminder", "Doctor appointment", Set.of(Tag.PERSONAL));

        mockMvc.perform(get("/api/v1/notes?tag=BUSINESS&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Business Meeting"));
    }

    @Test
    @DisplayName("Should return empty list when filtering by non-existent tag")
    void shouldReturnEmptyListWhenFilteringByNonExistentTag() throws Exception {
        createAndSaveNote("Business Note", "Some business text", Set.of(Tag.BUSINESS));

        mockMvc.perform(get("/api/v1/notes?tag=IMPORTANT&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // Get by ID tests
    @Test
    @DisplayName("Should get note by ID with full details")
    void shouldGetNoteByIdWithFullDetails() throws Exception {
        Note savedNote = createAndSaveNote(
                "Detailed Note",
                "This note contains all the details including the full text content.",
                Set.of(Tag.IMPORTANT)
        );

        mockMvc.perform(get("/api/v1/notes/" + savedNote.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedNote.getId()))
                .andExpect(jsonPath("$.title").value("Detailed Note"))
                .andExpect(jsonPath("$.text").value(containsString("full text content")))
                .andExpect(jsonPath("$.tags[0]").value("IMPORTANT"))
                .andExpect(jsonPath("$.createdDate").exists());
    }

    @Test
    @DisplayName("Should return 404 when note not found")
    void shouldReturn404WhenNoteNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/notes/nonexistent123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Note not found")));
    }

    // Update tests
    @Test
    @DisplayName("Should update note successfully")
    void shouldUpdateNoteSuccessfully() throws Exception {
        Note savedNote = createAndSaveNote(
                "Original Title",
                "Original text content",
                Set.of(Tag.PERSONAL)
        );

        NoteUpdateRequest updateRequest = new NoteUpdateRequest(
                "Updated Title - Final Version",
                "This is the updated text with new information and additional details.",
                Set.of(Tag.BUSINESS, Tag.IMPORTANT)
        );

        mockMvc.perform(put("/api/v1/notes/" + savedNote.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedNote.getId()))
                .andExpect(jsonPath("$.title").value("Updated Title - Final Version"))
                .andExpect(jsonPath("$.text").value(containsString("updated text")))
                .andExpect(jsonPath("$.tags", hasSize(2)));

        Note updatedNote = noteRepository.findById(savedNote.getId()).orElseThrow();
        Assertions.assertEquals("Updated Title - Final Version", updatedNote.getTitle());
        Assertions.assertEquals(2, updatedNote.getTags().size());
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent note")
    void shouldReturn404WhenUpdatingNonExistentNote() throws Exception {
        NoteUpdateRequest updateRequest = new NoteUpdateRequest(
                "Updated Title",
                "Updated text",
                Set.of(Tag.PERSONAL)
        );

        mockMvc.perform(put("/api/v1/notes/nonexistent456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Note not found")));
    }

    // Delete tests
    @Test
    @DisplayName("Should delete note successfully")
    void shouldDeleteNoteSuccessfully() throws Exception {
        Note savedNote = createAndSaveNote(
                "Note to Delete",
                "This note will be deleted",
                null
        );

        Assertions.assertTrue(noteRepository.existsById(savedNote.getId()));

        mockMvc.perform(delete("/api/v1/notes/" + savedNote.getId()))
                .andExpect(status().isNoContent());

        Assertions.assertFalse(noteRepository.existsById(savedNote.getId()));
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent note")
    void shouldReturn404WhenDeletingNonExistentNote() throws Exception {
        mockMvc.perform(delete("/api/v1/notes/nonexistent789"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Note not found")));
    }

    // Word statistics tests
    @Test
    @DisplayName("Should return word statistics for note")
    void shouldReturnWordStatistics() throws Exception {
        Note savedNote = createAndSaveNote(
                "Statistics Test",
                "note is just a note with some text and more text",
                null
        );

        mockMvc.perform(get("/api/v1/notes/" + savedNote.getId() + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value(2))
                .andExpect(jsonPath("$.text").value(2))
                .andExpect(jsonPath("$.is").value(1))
                .andExpect(jsonPath("$.just").value(1))
                .andExpect(jsonPath("$.a").value(1));
    }

    @Test
    @DisplayName("Should handle word statistics with case insensitive counting")
    void shouldHandleWordStatisticsCaseInsensitive() throws Exception {
        Note savedNote = createAndSaveNote(
                "Case Test",
                "Java JAVA java Spring SPRING spring",
                null
        );

        mockMvc.perform(get("/api/v1/notes/" + savedNote.getId() + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.java").value(3))
                .andExpect(jsonPath("$.spring").value(3));
    }

    @Test
    @DisplayName("Should handle Ukrainian text in word statistics")
    void shouldHandleUkrainianTextInStatistics() throws Exception {
        Note savedNote = createAndSaveNote(
                "Ukrainian Test",
                "Привіт світ привіт Україна",
                null
        );

        mockMvc.perform(get("/api/v1/notes/" + savedNote.getId() + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.привіт").value(2))
                .andExpect(jsonPath("$.світ").value(1))
                .andExpect(jsonPath("$.україна").value(1));
    }

    @Test
    @DisplayName("Should return empty statistics for note with only punctuation")
    void shouldReturnEmptyStatisticsForPunctuationOnly() throws Exception {
        Note savedNote = createAndSaveNote(
                "Punctuation Test",
                "!!! ??? ... ,,, ---",
                null
        );

        mockMvc.perform(get("/api/v1/notes/" + savedNote.getId() + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Should return 404 for statistics of non-existent note")
    void shouldReturn404ForStatisticsOfNonExistentNote() throws Exception {
        mockMvc.perform(get("/api/v1/notes/nonexistent999/stats"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Note not found")));
    }

    private Note createAndSaveNote(String title, String text, Set<Tag> tags) {
        return createAndSaveNote(title, text, tags, LocalDateTime.now());
    }
}