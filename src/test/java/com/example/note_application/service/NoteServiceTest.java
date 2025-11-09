package com.example.note_application.service;

import com.example.note_application.dto.NoteCreateRequest;
import com.example.note_application.dto.NoteDetailResponse;
import com.example.note_application.dto.NoteUpdateRequest;
import com.example.note_application.exception.NoteNotFoundException;
import com.example.note_application.model.Note;
import com.example.note_application.model.Tag;
import com.example.note_application.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    void getWordStatisticsWithSimpleText() {
        Note simpleNote = Note.builder()
                .id("507f1f77bcf86cd799439013")
                .title("Word Statistics Test")
                .text("note is just a note")
                .createdDate(LocalDateTime.now())
                .build();

        when(noteRepository.findById("507f1f77bcf86cd799439013")).thenReturn(Optional.of(simpleNote));

        Map<String, Long> stats = noteService.getWordStatistics("507f1f77bcf86cd799439013");

        assertNotNull(stats);
        assertEquals(2L, stats.get("note"));
        assertEquals(1L, stats.get("is"));
        assertEquals(1L, stats.get("just"));
        assertEquals(1L, stats.get("a"));
        assertEquals(4, stats.size());
    }

    @Test
    void getWordStatisticWithComplexText() {
        Note complexNote = Note.builder()
                .id("507f1f77bcf86cd799439014")
                .title("Complex Statistics Test")
                .text("The meeting was productive. The team discussed the project timeline. " +
                        "The project requires more resources. The deadline is approaching fast.")
                .createdDate(LocalDateTime.now())
                .build();

        when(noteRepository.findById("507f1f77bcf86cd799439014")).thenReturn(Optional.of(complexNote));

        Map<String, Long> stats = noteService.getWordStatistics("507f1f77bcf86cd799439014");

        assertNotNull(stats);
        assertEquals(5L, stats.get("the"));  // the most frequent word
        assertEquals(2L, stats.get("project"));
        assertEquals(1L, stats.get("meeting"));
        assertEquals(1L, stats.get("productive"));

        // check that the sorting works (the first word has the highest count)
        String firstWord = stats.keySet().iterator().next();
        assertEquals(5L, stats.get(firstWord));
    }

    @Test
    void NotFoundDuringGetWordStatistics() {
        when(noteRepository.findById("nonexistent999")).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () ->
                noteService.getWordStatistics("nonexistent999"));
    }

    @Test
    void getWordStatisticsCheckCaseInsensitive() {
        Note caseNote = Note.builder()
                .id("507f1f77bcf86cd799439015")
                .title("Case Test")
                .text("Java JAVA java Javascript javascript")
                .createdDate(LocalDateTime.now())
                .build();

        when(noteRepository.findById("507f1f77bcf86cd799439015")).thenReturn(Optional.of(caseNote));

        Map<String, Long> stats = noteService.getWordStatistics("507f1f77bcf86cd799439015");

        assertNotNull(stats);
        assertEquals(3L, stats.get("java"));  // All “Java” options must be combined
        assertEquals(2L, stats.get("javascript"));
    }
}