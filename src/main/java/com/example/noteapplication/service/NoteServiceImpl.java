package com.example.noteapplication.service;

import com.example.noteapplication.dto.NoteCreateRequest;
import com.example.noteapplication.dto.NoteDetailResponse;
import com.example.noteapplication.dto.NoteListResponse;
import com.example.noteapplication.dto.NoteUpdateRequest;
import com.example.noteapplication.exception.NoteNotFoundException;
import com.example.noteapplication.mapper.NoteMapper;
import com.example.noteapplication.model.Note;
import com.example.noteapplication.model.Tag;
import com.example.noteapplication.repository.NoteRepository;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;
    private final NoteMapper mapper;
    private static final Pattern NON_WORD_PATTERN = Pattern.compile("[^a-zа-яієїґ\\s]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    @Override
    public NoteDetailResponse createNote(NoteCreateRequest request) {
        Note note = Note.builder()
                .title(request.title())
                .text(request.text())
                .tags(request.tags() != null ? request.tags() : new HashSet<>())
                .createdDate(LocalDateTime.now())
                .build();
        Note savedNote = noteRepository.save(note);
        return mapper.mapToDetailResponse(savedNote);
    }

    @Override
    public NoteDetailResponse updateNote(String id, NoteUpdateRequest request) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + id));
        note.setTitle(request.title());
        note.setText(request.text());
        note.setTags(request.tags() != null ? request.tags() : note.getTags());

        Note updatedNote = noteRepository.save(note);
        return mapper.mapToDetailResponse(updatedNote);
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
        Page<Note> notes = tag != null
                ? noteRepository.findByTagsContainingOrderByCreatedDateDesc(tag, pageable)
                : noteRepository.findAllByOrderByCreatedDateDesc(pageable);
        return notes.map(mapper::mapToListResponse);
    }

    @Override
    public NoteDetailResponse getNoteById(String id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + id));
        return mapper.mapToDetailResponse(note);
    }

    @Override
    public Map<String, Long> getWordStatistics(String id) {
        Note note = noteRepository.findById(id).orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + id));
        String text = note.getText().toLowerCase();
        String cleanedText = NON_WORD_PATTERN.matcher(text).replaceAll("");
        if (cleanedText.isEmpty()) {return new LinkedHashMap<>();}
        String[] words = WHITESPACE_PATTERN.split(cleanedText);
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
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}
