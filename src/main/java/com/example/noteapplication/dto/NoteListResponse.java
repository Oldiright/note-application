package com.example.noteapplication.dto;


import java.time.LocalDateTime;

public record NoteListResponse(
        String id,
        String title,
        LocalDateTime createdDate
) {
}