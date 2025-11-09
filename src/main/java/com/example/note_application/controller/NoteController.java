package com.example.note_application.controller;

import com.example.note_application.dto.NoteCreateRequest;
import com.example.note_application.dto.NoteDetailResponse;
import com.example.note_application.dto.NoteListResponse;
import com.example.note_application.dto.NoteUpdateRequest;
import com.example.note_application.model.Tag;
import com.example.note_application.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<NoteDetailResponse> createNote(@Valid @RequestBody NoteCreateRequest request) {
        NoteDetailResponse response = noteService.createNote(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PutMapping("/{id}")
    public ResponseEntity<NoteDetailResponse> updateNote(
            @PathVariable String id,
            @Valid @RequestBody NoteUpdateRequest request) {
        NoteDetailResponse response = noteService.updateNote(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable String id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<NoteListResponse>> listNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Tag tag) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NoteListResponse> response = noteService.listNotes(pageable, tag);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteDetailResponse> getNoteById(@PathVariable String id) {
        NoteDetailResponse response = noteService.getNoteById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Long>> getWordStatistics(@PathVariable String id) {
        Map<String, Long> stats = noteService.getWordStatistics(id);
        return ResponseEntity.ok(stats);
    }

}