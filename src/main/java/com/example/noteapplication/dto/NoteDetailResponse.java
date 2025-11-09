package com.example.noteapplication.dto;


import com.example.noteapplication.model.Tag;

import java.time.LocalDateTime;
import java.util.Set;

public record NoteDetailResponse(
        String id,
        String title,
        LocalDateTime createdDate,
        String text,
        Set<Tag> tags
) {
}
