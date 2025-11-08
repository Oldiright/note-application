package com.example.note_application.service;

import com.example.note_application.dto.NoteCreateRequest;
import com.example.note_application.dto.NoteDetailResponse;
import com.example.note_application.dto.NoteListResponse;
import com.example.note_application.dto.NoteUpdateRequest;
import com.example.note_application.model.Note;
import com.example.note_application.model.Tag;
import com.example.note_application.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;

    @Override
    public NoteDetailResponse createNote(NoteCreateRequest request) {
        Note note = Note.builder()
                .title(request.title())
                .text(request.text())
                .tags(request.tags() != null ? request.tags() : new HashSet<>())
                .createdDate(LocalDateTime.now())
                .build();
        Note savedNote = noteRepository.save(note);
        return null;
    }

    @Override
    public NoteDetailResponse updateNote(String id, NoteUpdateRequest request) {
        return null;
    }

    @Override
    public void deleteNote(String id) {

    }

    @Override
    public Page<NoteListResponse> listNotes(Pageable pageable, Tag tag) {
        return null;
    }

    @Override
    public NoteDetailResponse getNoteById(String id) {
        return null;
    }

    @Override
    public Map<String, Long> getWordStatistics(String id) {
        return null;
    }
}
