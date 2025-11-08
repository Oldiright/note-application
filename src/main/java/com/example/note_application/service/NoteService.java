package com.example.note_application.service;

import com.example.note_application.dto.NoteCreateRequest;
import com.example.note_application.dto.NoteDetailResponse;
import com.example.note_application.dto.NoteListResponse;
import com.example.note_application.dto.NoteUpdateRequest;
import com.example.note_application.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface NoteService {
    NoteDetailResponse createNote(NoteCreateRequest request);

    NoteDetailResponse updateNote(String id, NoteUpdateRequest request);

    void deleteNote(String id);

    Page<NoteListResponse> listNotes(Pageable pageable, Tag tag);

    NoteDetailResponse getNoteById(String id);

    Map<String, Long> getWordStatistics(String id);
}
