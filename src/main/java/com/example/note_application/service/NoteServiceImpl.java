package com.example.note_application.service;

import com.example.note_application.dto.NoteCreateRequest;
import com.example.note_application.dto.NoteDetailResponse;
import com.example.note_application.dto.NoteListResponse;
import com.example.note_application.dto.NoteUpdateRequest;
import com.example.note_application.exception.NoteNotFoundException;
import com.example.note_application.model.Note;
import com.example.note_application.model.Tag;
import com.example.note_application.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        return mapToDetailResponse(savedNote);
    }

    @Override
    public NoteDetailResponse updateNote(String id, NoteUpdateRequest request) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + id));
        note.setTitle(request.title());
        note.setText(request.text());
        note.setTags(request.tags() != null ? request.tags() : new HashSet<>());

        Note updatedNote = noteRepository.save(note);
        return mapToDetailResponse(updatedNote);
    }

    @Override
    public void deleteNote(String id) {
        if (!noteRepository.existsById(id)) {
            throw new NoteNotFoundException("Note not found with id: " + id);
        }
        noteRepository.deleteById(id);

    }

    @Override
    public Page<NoteListResponse> listNotes(Pageable pageable, Tag tag) {
        Page<Note> notes;
        if(tag != null) {
            notes = noteRepository.findByTagsContainingOrderByCreatedDateDesc(tag, pageable);
        } else {
            notes = noteRepository.findAllByOrderByCreatedDateDesc(pageable);
        }
        return notes.map(this::mapToListResponse);
    }

    @Override
    public NoteDetailResponse getNoteById(String id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + id));
        return mapToDetailResponse(note);
    }

    @Override
    public Map<String, Long> getWordStatistics(String id) {
        Note note =  noteRepository.findById(id).orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + id));
        String text = note.getText().toLowerCase();
        String cleanedText = text.replaceAll("[^a-zа-яієїґ\\s]", "");
        String[] words = cleanedText.split("\\s+");
        return Arrays.stream(words)
                .filter(word -> !word.isEmpty())
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry ::getKey,
                        Map.Entry ::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    private NoteDetailResponse mapToDetailResponse(Note note) {
        return new NoteDetailResponse (
                note.getId(),
                note.getTitle(),
                note.getCreatedDate(),
                note.getText(),
                note.getTags()
        );
    }
    private NoteListResponse mapToListResponse(Note note) {
        return new NoteListResponse (
                note.getId(),
                note.getTitle(),
                note.getCreatedDate()
        );
    }
}
