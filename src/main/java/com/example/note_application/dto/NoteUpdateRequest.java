package com.example.note_application.dto;

import com.example.note_application.model.Tag;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record NoteUpdateRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Text is required")
        String text,

        Set<Tag> tags
) {}