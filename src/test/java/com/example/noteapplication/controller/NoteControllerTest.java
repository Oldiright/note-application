package com.example.noteapplication.controller;

import com.example.noteapplication.dto.NoteCreateRequest;
import com.example.noteapplication.dto.NoteDetailResponse;
import com.example.noteapplication.dto.NoteUpdateRequest;
import com.example.noteapplication.model.Tag;
import com.example.noteapplication.service.NoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
class NoteControllerTest {
    private static final String GENERAL_PATH = "/api/v1/notes";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private NoteService noteService;

    @Test
    void successfulNoteCreate() throws Exception {
        NoteCreateRequest request = new NoteCreateRequest(
                "Test Note",
                "Test text",
                Set.of(Tag.PERSONAL)
        );
        NoteDetailResponse response = new NoteDetailResponse(
                "1",
                "Test Note",
                LocalDateTime.now(),
                "Test text",
                Set.of(Tag.PERSONAL)
        );
        when(noteService.createNote(any(NoteCreateRequest.class))).thenReturn(response);
        mockMvc.perform(post(GENERAL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andExpect(jsonPath("$.text").value("Test text"));
    }

    @Test
    void ValidationErrorDuringCreateNoteDueMissingTitle() throws Exception {
        NoteCreateRequest request = new NoteCreateRequest(
                null,
                "Test text",
                null
        );
        mockMvc.perform(post(GENERAL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void ValidationErrorDuringCreateNoteDueMissingText() throws Exception {
        NoteCreateRequest request = new NoteCreateRequest(
                "Test Note",
                null,
                null
        );
        mockMvc.perform(post(GENERAL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.text").exists());
    }

    @Test
    void successfulNoteUpdate() throws Exception {
        NoteUpdateRequest request = new NoteUpdateRequest(
                "Updated Note",
                "Updated text",
                Set.of(Tag.BUSINESS)
        );
        NoteDetailResponse response = new NoteDetailResponse(
                "1",
                "Updated Note",
                LocalDateTime.now(),
                "Updated text",
                Set.of(Tag.BUSINESS)
        );

        when(noteService.updateNote(eq("1"), any(NoteUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put(GENERAL_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Note"))
                .andExpect(jsonPath("$.text").value("Updated text"));
    }

    @Test
    void successfulNoteDelete() throws Exception {
        mockMvc.perform(delete(GENERAL_PATH + "/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void successfulGetNoteById() throws Exception {
        NoteDetailResponse response = new NoteDetailResponse(
                "1",
                "Test Note",
                LocalDateTime.now(),
                "Test text",
                Set.of(Tag.PERSONAL)
        );
        when(noteService.getNoteById("1")).thenReturn(response);

        mockMvc.perform(get(GENERAL_PATH + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andExpect(jsonPath("$.text").value("Test text"));
    }

    @Test
    void successfulGetWordStatistics() throws Exception {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("note", 2L);
        stats.put("is", 1L);
        stats.put("just", 1L);
        stats.put("a", 1L);

        when(noteService.getWordStatistics("1")).thenReturn(stats);

        mockMvc.perform(get(GENERAL_PATH + "/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value(2))
                .andExpect(jsonPath("$.is").value(1))
                .andExpect(jsonPath("$.just").value(1))
                .andExpect(jsonPath("$.a").value(1));
    }

    @Test
    void listNotesWithPagination() throws Exception {
        mockMvc.perform(get(GENERAL_PATH)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void listNotesWithTagFilter() throws Exception {
        mockMvc.perform(get(GENERAL_PATH)
                        .param("tag", "PERSONAL")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}