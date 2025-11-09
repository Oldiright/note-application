package com.example.noteapplication.dto;

import com.example.noteapplication.model.Tag;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record NoteCreateRequest(
        @NotBlank(message = "Title is required")
        String title,
        @NotBlank(message = "Text is required")
        String text,
        Set<Tag> tags
) {
}