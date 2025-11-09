package com.example.noteapplication.service;

import com.example.noteapplication.dto.NoteCreateRequest;
import com.example.noteapplication.dto.NoteDetailResponse;
import com.example.noteapplication.dto.NoteUpdateRequest;
import com.example.noteapplication.exception.NoteNotFoundException;
import com.example.noteapplication.model.Note;
import com.example.noteapplication.model.Tag;
import com.example.noteapplication.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;
    @InjectMocks
    private NoteServiceImpl noteService;
    private Note testNote;
    private NoteCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testNote = Note.builder()
                .id("507f1f77bcf86cd799439011")
                .title("Quarterly Business Review Meeting")
                .text("Prepare presentation slides for Q4 business review. Include revenue metrics, " +
                        "customer acquisition data, and market analysis. Schedule follow-up meetings with " +
                        "department heads to discuss strategic initiatives for the upcoming quarter.")
                .tags(Set.of(Tag.BUSINESS, Tag.IMPORTANT))
                .createdDate(LocalDateTime.of(2024, 11, 9, 14, 30))
                .build();

        createRequest = new NoteCreateRequest(
                "Quarterly Business Review Meeting",
                "Prepare presentation slides for Q4 business review. Include revenue metrics, " +
                        "customer acquisition data, and market analysis. Schedule follow-up meetings with " +
                        "department heads to discuss strategic initiatives for the upcoming quarter.",
                Set.of(Tag.BUSINESS, Tag.IMPORTANT)
        );
    }

    @Test
    void successfulCreateOfTheNote() {
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        NoteDetailResponse response = noteService.createNote(createRequest);

        assertNotNull(response);
        assertEquals("Quarterly Business Review Meeting", response.title());
        assertTrue(response.text().contains("Q4 business review"));
        assertTrue(response.text().contains("revenue metrics"));
        assertEquals(Set.of(Tag.BUSINESS, Tag.IMPORTANT), response.tags());
        assertNotNull(response.createdDate());
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void successfulCreateOfNoteWithEmptyTags() {
        NoteCreateRequest requestWithoutTags = new NoteCreateRequest(
                "Personal Reminder",
                "Don't forget to call mom this weekend and wish her happy birthday. " +
                        "Also need to pick up groceries on the way home.",
                null
        );
        Note noteWithoutTags = Note.builder()
                .id("507f1f77bcf86cd799439012")
                .title("Personal Reminder")
                .text("Don't forget to call mom this weekend and wish her happy birthday. " +
                        "Also need to pick up groceries on the way home.")
                .tags(Set.of())
                .createdDate(LocalDateTime.now())
                .build();
        when(noteRepository.save(any(Note.class))).thenReturn(noteWithoutTags);

        NoteDetailResponse response = noteService.createNote(requestWithoutTags);

        assertNotNull(response);
        assertEquals("Personal Reminder", response.title());
        assertTrue(response.tags().isEmpty());
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void successfulUpdateOfTheNote() {
        NoteUpdateRequest updateRequest = new NoteUpdateRequest(
                "Updated Q4 Business Review - Final Version",
                "Completed presentation with final revenue numbers: $2.5M quarterly revenue, " +
                        "25% growth in customer base, successful product launch metrics. Added competitive " +
                        "analysis section and updated market positioning strategy. Ready for executive review.",
                Set.of(Tag.BUSINESS, Tag.IMPORTANT)
        );
        Note updatedNote = Note.builder()
                .id("507f1f77bcf86cd799439011")
                .title("Updated Q4 Business Review - Final Version")
                .text("Completed presentation with final revenue numbers: $2.5M quarterly revenue, " +
                        "25% growth in customer base, successful product launch metrics. Added competitive " +
                        "analysis section and updated market positioning strategy. Ready for executive review.")
                .tags(Set.of(Tag.BUSINESS, Tag.IMPORTANT))
                .createdDate(testNote.getCreatedDate())
                .build();
        when(noteRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(updatedNote);

        NoteDetailResponse response = noteService.updateNote("507f1f77bcf86cd799439011", updateRequest);

        assertNotNull(response);
        assertEquals("Updated Q4 Business Review - Final Version", response.title());
        assertTrue(response.text().contains("$2.5M quarterly revenue"));
        assertTrue(response.text().contains("25% growth"));
        assertTrue(response.text().contains("executive review"));
        assertEquals(Set.of(Tag.BUSINESS, Tag.IMPORTANT), response.tags());
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void noteNotFoundDuringUpdate() {
        NoteUpdateRequest updateRequest = new NoteUpdateRequest(
                "Non-existent Note Title",
                "This note doesn't exist in the database and should trigger an exception.",
                Set.of(Tag.PERSONAL)
        );
        when(noteRepository.findById("nonexistent123")).thenReturn(Optional.empty());

        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.updateNote("nonexistent123", updateRequest)
        );
        assertTrue(exception.getMessage().contains("Note not found with id: nonexistent123"));
    }

    @Test
    void successfulNoteDelete() {
        when(noteRepository.existsById("507f1f77bcf86cd799439011")).thenReturn(true);
        doNothing().when(noteRepository).deleteById("507f1f77bcf86cd799439011");

        assertDoesNotThrow(() -> noteService.deleteNote("507f1f77bcf86cd799439011"));
        verify(noteRepository, times(1)).deleteById("507f1f77bcf86cd799439011");
    }

    @Test
    void NoteNotFoundDuringDelete() {
        when(noteRepository.existsById("nonexistent456")).thenReturn(false);

        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.deleteNote("nonexistent456")
        );
        assertTrue(exception.getMessage().contains("Note not found with id: nonexistent456"));
    }

    @Test
    void successfulGetNoteById() {
        when(noteRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.of(testNote));

        NoteDetailResponse response = noteService.getNoteById("507f1f77bcf86cd799439011");

        assertNotNull(response);
        assertEquals("507f1f77bcf86cd799439011", response.id());
        assertEquals("Quarterly Business Review Meeting", response.title());
        assertTrue(response.text().contains("Q4 business review"));
        assertEquals(Set.of(Tag.BUSINESS, Tag.IMPORTANT), response.tags());
    }

    @Test
    void NotFoundDuringGetNoteById() {
        when(noteRepository.findById("nonexistent789")).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () ->
                noteService.getNoteById("nonexistent789"));
    }

    @Test
    void NotFoundDuringGetWordStatistics() {
        when(noteRepository.findById("nonexistent999")).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () ->
                noteService.getWordStatistics("nonexistent999"));
    }

    @ParameterizedTest(name = "[{index}] text=''{0}'' should return correct statistics")
    @MethodSource("provideWordStatisticsTestCases")
    void getWordStatistics_variousCases(String testId, String text, Map<String, Long> expectedStats) {
        // Arrange
        Note note = Note.builder()
                .id(testId)
                .title("Test Note")
                .text(text)
                .createdDate(LocalDateTime.now())
                .build();

        when(noteRepository.findById(testId)).thenReturn(Optional.of(note));

        // Act
        Map<String, Long> result = noteService.getWordStatistics(testId);

        // Assert
        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedStats);
        verify(noteRepository, times(1)).findById(testId);
    }

    private static Stream<Arguments> provideWordStatisticsTestCases() {
        return Stream.of(
                // Simple text - repeated word
                Arguments.of(
                        "test-simple",
                        "note is just a note",
                        Map.of("note", 2L, "is", 1L, "just", 1L, "a", 1L)
                ),

                // Complex text - multiple repeated words
                Arguments.of(
                        "test-complex",
                        "The meeting was productive. The team discussed the project timeline. " +
                                "The project requires more resources. The deadline is approaching fast.",
                        createOrderedMap(
                                "the", 5L,
                                "project", 2L,
                                "meeting", 1L,
                                "was", 1L,
                                "productive", 1L,
                                "team", 1L,
                                "discussed", 1L,
                                "timeline", 1L,
                                "requires", 1L,
                                "more", 1L,
                                "resources", 1L,
                                "deadline", 1L,
                                "is", 1L,
                                "approaching", 1L,
                                "fast", 1L
                        )
                ),

                // Case insensitive - Latin
                Arguments.of(
                        "test-case-latin",
                        "Java JAVA java Javascript javascript",
                        Map.of("java", 3L, "javascript", 2L)
                ),

                // Case insensitive - Cyrillic
                Arguments.of(
                        "test-case-cyrillic",
                        "Київ КИЇВ київ Україна УКРАЇНА україна",
                        Map.of("київ", 3L, "україна", 3L)
                ),

                // Mixed Cyrillic and Latin
                Arguments.of(
                        "test-mixed",
                        "Spring Boot додаток на Java для бізнесу",
                        Map.of(
                                "spring", 1L,
                                "boot", 1L,
                                "додаток", 1L,
                                "на", 1L,
                                "java", 1L,
                                "для", 1L,
                                "бізнесу", 1L
                        )
                ),

                // Real Ukrainian sentence
                Arguments.of(
                        "test-ukrainian-sentence",
                        "Щоденна нарада команди щодо проєкту. Команда обговорила прогрес проєкту.",
                        Map.of(
                                "щоденна", 1L,
                                "нарада", 1L,
                                "команди", 1L,
                                "щодо", 1L,
                                "проєкту", 2L,
                                "команда", 1L,
                                "обговорила", 1L,
                                "прогрес", 1L
                        )
                ),

                // Ukrainian with punctuation
                Arguments.of(
                        "test-ukrainian-punctuation",
                        "Привіт, світ! Це тестовий текст. Світ прекрасний!",
                        Map.of(
                                "привіт", 1L,
                                "світ", 2L,
                                "це", 1L,
                                "тестовий", 1L,
                                "текст", 1L,
                                "прекрасний", 1L
                        )
                ),

                // Edge cases
                Arguments.of("test-empty", "", Collections.emptyMap()),
                Arguments.of("test-spaces", "   ", Collections.emptyMap()),
                Arguments.of("test-single-latin", "hello", Map.of("hello", 1L)),
                Arguments.of("test-single-cyrillic", "привіт", Map.of("привіт", 1L)),

                // Special characters only
                Arguments.of("test-special-chars", "!!! ??? ...", Collections.emptyMap())
        );
    }

    private static Map<String, Long> createOrderedMap(Object... keyValuePairs) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            map.put((String) keyValuePairs[i], (Long) keyValuePairs[i + 1]);
        }
        return map;
    }

}