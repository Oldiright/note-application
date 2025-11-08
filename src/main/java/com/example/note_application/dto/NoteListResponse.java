package com.example.note_application.dto;


import java.time.LocalDateTime;
public record NoteListResponse(
        String id,
        String title,
        LocalDateTime createdDate
) {}