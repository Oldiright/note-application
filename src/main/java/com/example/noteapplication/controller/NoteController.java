package com.example.noteapplication.controller;

import com.example.noteapplication.dto.NoteCreateRequest;
import com.example.noteapplication.dto.NoteDetailResponse;
import com.example.noteapplication.dto.NoteListResponse;
import com.example.noteapplication.dto.NoteUpdateRequest;
import com.example.noteapplication.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Notes", description = "API for managing personal notes with tags and word statistics")
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    @Operation(
            summary = "Create a new note",
            description = "Creates a new note with title, text, and optional tags. Title and text are required fields."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Note created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteDetailResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "507f1f77bcf86cd799439011",
                                      "title": "Meeting Notes",
                                      "createdDate": "2024-11-09T14:30:00",
                                      "text": "Discuss project timeline and budget allocation",
                                      "tags": ["BUSINESS", "IMPORTANT"]
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - title or text is missing",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "errors": {
                                        "title": "Title is required"
                                      },
                                      "timestamp": "2024-11-09T14:30:00"
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<NoteDetailResponse> createNote(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Note creation request",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = NoteCreateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "title": "Meeting Notes",
                                      "text": "Discuss project timeline and budget allocation",
                                      "tags": ["BUSINESS", "IMPORTANT"]
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody NoteCreateRequest request) {
        NoteDetailResponse response = noteService.createNote(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing note",
            description = "Updates all fields of an existing note. Title and text are required. Pay attention, " +
                    "you can't delete all the tags after creating."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Note updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteDetailResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 404,
                                      "message": "Note not found with id: 507f1f77bcf86cd799439011",
                                      "timestamp": "2024-11-09T14:30:00"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data"
            )
    })
    public ResponseEntity<NoteDetailResponse> updateNote(
            @Parameter(description = "ID of the note to update", required = true, example = "507f1f77bcf86cd799439011")
            @PathVariable String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Note update request",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = NoteUpdateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "title": "Updated Meeting Notes",
                                      "text": "Updated project timeline and new budget allocation",
                                      "tags": ["BUSINESS"]
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody NoteUpdateRequest request) {
        NoteDetailResponse response = noteService.updateNote(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a note",
            description = "Permanently deletes a note by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Note deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 404,
                                      "message": "Note not found with id: 507f1f77bcf86cd799439011",
                                      "timestamp": "2024-11-09T14:30:00"
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<Void> deleteNote(
            @Parameter(description = "ID of the note to delete", required = true, example = "507f1f77bcf86cd799439011")
            @PathVariable String id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            summary = "List all notes",
            description = "Returns a paginated list of notes showing only title and created date. " +
                    "Notes are sorted by creation date (newest first). Supports optional filtering by tag."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of notes retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "content": [
                                        {
                                          "id": "507f1f77bcf86cd799439011",
                                          "title": "Meeting Notes",
                                          "createdDate": "2024-11-09T14:30:00"
                                        }
                                      ],
                                      "pageable": {
                                        "pageNumber": 0,
                                        "pageSize": 10
                                      },
                                      "totalElements": 1,
                                      "totalPages": 1
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<Page<NoteListResponse>> listNotes(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(
                    description = "Filter notes by tag. Allowed values: BUSINESS, PERSONAL, IMPORTANT",
                    example = "BUSINESS"
            )
            @RequestParam(required = false) com.example.noteapplication.model.Tag tag) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NoteListResponse> response = noteService.listNotes(pageable, tag);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get note by ID",
            description = "Retrieves full note details including the text content. " +
                    "Use this endpoint to get the complete note after selecting from the list."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Note found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteDetailResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "507f1f77bcf86cd799439011",
                                      "title": "Meeting Notes",
                                      "createdDate": "2024-11-09T14:30:00",
                                      "text": "Discuss project timeline and budget allocation",
                                      "tags": ["BUSINESS", "IMPORTANT"]
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found"
            )
    })
    public ResponseEntity<NoteDetailResponse> getNoteById(
            @Parameter(description = "ID of the note to retrieve", required = true, example = "507f1f77bcf86cd799439011")
            @PathVariable String id) {
        NoteDetailResponse response = noteService.getNoteById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/stats")
    @Operation(
            summary = "Get word statistics for a note",
            description = "Returns word frequency statistics for the note's text. " +
                    "Words are counted case-insensitively and sorted by frequency (descending). " +
                    "Only alphabetic characters are counted as words."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistics calculated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "note": 2,
                                      "is": 1,
                                      "just": 1,
                                      "a": 1
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found"
            )
    })
    public ResponseEntity<Map<String, Long>> getWordStatistics(
            @Parameter(description = "ID of the note to analyze", required = true, example = "507f1f77bcf86cd799439011")
            @PathVariable String id) {
        Map<String, Long> stats = noteService.getWordStatistics(id);
        return ResponseEntity.ok(stats);
    }
}