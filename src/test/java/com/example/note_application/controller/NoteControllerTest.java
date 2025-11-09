package com.example.note_application.controller;

import com.example.note_application.dto.NoteCreateRequest;
import com.example.note_application.dto.NoteDetailResponse;
import com.example.note_application.model.Tag;
import com.example.note_application.service.NoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NoteService noteService;

    @Test
    void successfulNoteCreate() throws Exception {
        NoteCreateRequest request = new NoteCreateRequest (
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

        mockMvc.perform(post("/api/v1/notes")
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
        mockMvc.perform(post("/api/v1/notes")
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
        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.text").exists());
    }


}