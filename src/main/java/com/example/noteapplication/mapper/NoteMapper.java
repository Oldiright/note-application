package com.example.noteapplication.mapper;

import com.example.noteapplication.dto.NoteDetailResponse;
import com.example.noteapplication.dto.NoteListResponse;
import com.example.noteapplication.model.Note;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {
    public NoteDetailResponse mapToDetailResponse(Note note) {
        return new NoteDetailResponse(
                note.getId(),
                note.getTitle(),
                note.getCreatedDate(),
                note.getText(),
                note.getTags()
        );
    }

    public NoteListResponse mapToListResponse(Note note) {
        return new NoteListResponse(
                note.getId(),
                note.getTitle(),
                note.getCreatedDate()
        );
    }
}
