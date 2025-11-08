package com.example.note_application.dto;


import com.example.note_application.model.Tag;

import java.time.LocalDateTime;
import java.util.Set;

public record NoteDetailResponse(
        String id,
        String title,
        LocalDateTime createdDate,
        String text,
        Set<Tag> tags
) {}
