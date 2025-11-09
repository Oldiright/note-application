package com.example.noteapplication.service;

import com.example.noteapplication.dto.NoteCreateRequest;
import com.example.noteapplication.dto.NoteDetailResponse;
import com.example.noteapplication.dto.NoteListResponse;
import com.example.noteapplication.dto.NoteUpdateRequest;
import com.example.noteapplication.model.Tag;
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
