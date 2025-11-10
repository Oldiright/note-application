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
@io.swagger.v3.oas.annotations.tags.Tag(
        name = "Notes API",
        description = "REST API for managing personal notes with tags and word statistics. " +
                "Supports CRUD operations, pagination, filtering by tags, and text analysis."
)
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    @Operation(
            summary = "Create a new note",
            description = "Creates a new note with title, text, and optional tags. " +
                    "Title and text are required fields with minimum length of 1 character. " +
                    "Tags are optional and can include: BUSINESS, PERSONAL, IMPORTANT."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Note created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteDetailResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful creation",
                                    value = """
                                    {
                                      "id": "507f1f77bcf86cd799439011",
                                      "title": "Meeting Notes",
                                      "createdDate": "2024-11-09T14:30:00",
                                      "text": "Discuss project timeline and budget allocation",
                                      "tags": ["BUSINESS", "IMPORTANT"]
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation failed or invalid input",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Validation errors",
                                            description = "When required fields are missing or empty",
                                            value = """
                                            {
                                              "status": 400,
                                              "errors": {
                                                "title": "Title is required",
                                                "text": "Text is required"
                                              },
                                              "timestamp": "2024-11-09T14:30:00"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid tag enum",
                                            description = "When an invalid tag value is provided",
                                            value = """
                                            {
                                              "status": 400,
                                              "message": "Invalid value 'URGENT' for field 'tags'. Allowed values are: [BUSINESS, PERSONAL, IMPORTANT]",
                                              "timestamp": "2024-11-09T14:30:00"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Malformed JSON",
                                            description = "When request body is not valid JSON",
                                            value = """
                                            {
                                              "status": 400,
                                              "message": "Invalid request body",
                                              "timestamp": "2024-11-09T14:30:00"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Unexpected error",
                                    value = """
                                    {
                                      "status": 500,
                                      "message": "An unexpected error occurred: Database connection failed",
                                      "timestamp": "2024-11-09T14:30:00"
                                    }
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<NoteDetailResponse> createNote(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Note creation request with required title and text fields",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = NoteCreateRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Complete note",
                                            value = """
                                            {
                                              "title": "Meeting Notes",
                                              "text": "Discuss project timeline and budget allocation",
                                              "tags": ["BUSINESS", "IMPORTANT"]
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Note without tags",
                                            value = """
                                            {
                                              "title": "Quick reminder",
                                              "text": "Call the client tomorrow at 10 AM"
                                            }
                                            """
                                    )
                            }
                    )
            )
            @Valid @RequestBody NoteCreateRequest request) {
        NoteDetailResponse response = noteService.createNote(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing note",
            description = "Updates all fields of an existing note. Title and text are required fields. " +
                    "Tags field is optional - if not provided, existing tags will be preserved. " +
                    "Note: Once a note has tags, at least one tag must be retained (cannot remove all tags)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Note updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteDetailResponse.class),
                            examples = @ExampleObject(
                                    name = "Updated note",
                                    value = """
                                    {
                                      "id": "507f1f77bcf86cd799439011",
                                      "title": "Updated Meeting Notes",
                                      "createdDate": "2024-11-09T14:30:00",
                                      "text": "Updated project timeline and new budget allocation",
                                      "tags": ["BUSINESS"]
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation failed or invalid input",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Validation errors",
                                            value = """
                                            {
                                              "status": 400,
                                              "errors": {
                                                "title": "Title is required",
                                                "text": "Text is required"
                                              },
                                              "timestamp": "2024-11-09T14:30:00"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid tag",
                                            value = """
                                            {
                                              "status": 400,
                                              "message": "Invalid value 'WORK' for field 'tags'. Allowed values are: [BUSINESS, PERSONAL, IMPORTANT]",
                                              "timestamp": "2024-11-09T14:30:00"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Note not found",
                                    value = """
                                    {
                                      "status": 404,
                                      "message": "Note not found with id: 507f1f77bcf86cd799439011",
                                      "timestamp": "2024-11-09T14:30:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 500,
                                      "message": "An unexpected error occurred: Database error",
                                      "timestamp": "2024-11-09T14:30:00"
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<NoteDetailResponse> updateNote(
            @Parameter(
                    description = "MongoDB ObjectId of the note to update",
                    required = true,
                    example = "507f1f77bcf86cd799439011"
            )
            @PathVariable String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Note update request with all fields",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = NoteUpdateRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Update all fields",
                                            value = """
                                            {
                                              "title": "Updated Meeting Notes",
                                              "text": "Updated project timeline and new budget allocation",
                                              "tags": ["BUSINESS", "IMPORTANT"]
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Update without changing tags",
                                            description = "Tags will remain unchanged if not provided",
                                            value = """
                                            {
                                              "title": "Updated Title",
                                              "text": "New content for the note"
                                            }
                                            """
                                    )
                            }
                    )
            )
            @Valid @RequestBody NoteUpdateRequest request) {
        NoteDetailResponse response = noteService.updateNote(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a note",
            description = "Permanently deletes a note by its ID. This operation cannot be undone."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Note deleted successfully (no content returned)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Note not found",
                                    value = """
                                    {
                                      "status": 404,
                                      "message": "Note not found with id: 507f1f77bcf86cd799439011",
                                      "timestamp": "2024-11-09T14:30:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 500,
                                      "message": "An unexpected error occurred: Unable to delete note",
                                      "timestamp": "2024-11-09T14:30:00"
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<Void> deleteNote(
            @Parameter(
                    description = "MongoDB ObjectId of the note to delete",
                    required = true,
                    example = "507f1f77bcf86cd799439011"
            )
            @PathVariable String id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            summary = "List all notes",
            description = "Returns a paginated list of notes showing only basic information (id, title, created date). " +
                    "Notes are sorted by creation date in descending order (newest first). " +
                    "Supports optional filtering by tag. Use the detailed endpoint to get full note content."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of notes retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Paginated list",
                                            value = """
                                            {
                                              "content": [
                                                {
                                                  "id": "507f1f77bcf86cd799439011",
                                                  "title": "Meeting Notes",
                                                  "createdDate": "2024-11-09T14:30:00"
                                                },
                                                {
                                                  "id": "507f1f77bcf86cd799439012",
                                                  "title": "Project Ideas",
                                                  "createdDate": "2024-11-08T10:15:00"
                                                }
                                              ],
                                              "pageable": {
                                                "pageNumber": 0,
                                                "pageSize": 10,
                                                "sort": {
                                                  "sorted": true,
                                                  "ascending": false
                                                }
                                              },
                                              "totalElements": 25,
                                              "totalPages": 3,
                                              "first": true,
                                              "last": false,
                                              "numberOfElements": 10
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Empty list",
                                            value = """
                                            {
                                              "content": [],
                                              "pageable": {
                                                "pageNumber": 0,
                                                "pageSize": 10
                                              },
                                              "totalElements": 0,
                                              "totalPages": 0,
                                              "first": true,
                                              "last": true,
                                              "numberOfElements": 0
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid tag parameter",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Invalid tag filter",
                                    value = """
                                    {
                                      "status": 400,
                                      "message": "Invalid value 'URGENT' for field 'tag'. Allowed values are: [BUSINESS, PERSONAL, IMPORTANT]",
                                      "timestamp": "2024-11-09T14:30:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 500,
                                      "message": "An unexpected error occurred: Database connection failed",
                                      "timestamp": "2024-11-09T14:30:00"
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<Page<NoteListResponse>> listNotes(
            @Parameter(
                    description = "Page number (0-based indexing)",
                    example = "0",
                    schema = @Schema(minimum = "0", defaultValue = "0")
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Number of items per page",
                    example = "10",
                    schema = @Schema(minimum = "1", maximum = "100", defaultValue = "10")
            )
            @RequestParam(defaultValue = "10") int size,

            @Parameter(
                    description = "Filter notes by tag. Only notes containing this tag will be returned.",
                    example = "BUSINESS",
                    schema = @Schema(
                            allowableValues = {"BUSINESS", "PERSONAL", "IMPORTANT"}
                    )
            )
            @RequestParam(required = false) com.example.noteapplication.model.Tag tag) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NoteListResponse> response = noteService.listNotes(pageable, tag);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get note details by ID",
            description = "Retrieves complete note information including the full text content and all tags. " +
                    "Use this endpoint to get the complete note details after selecting from the paginated list."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Note found and returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteDetailResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Note with tags",
                                            value = """
                                            {
                                              "id": "507f1f77bcf86cd799439011",
                                              "title": "Meeting Notes",
                                              "createdDate": "2024-11-09T14:30:00",
                                              "text": "Discuss project timeline and budget allocation. Key points:\\n1. Timeline: 6 months\\n2. Budget: $100,000\\n3. Team size: 5 developers",
                                              "tags": ["BUSINESS", "IMPORTANT"]
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Note without tags",
                                            value = """
                                            {
                                              "id": "507f1f77bcf86cd799439012",
                                              "title": "Shopping List",
                                              "createdDate": "2024-11-10T09:00:00",
                                              "text": "Milk, Bread, Eggs, Coffee",
                                              "tags": []
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Note not found",
                                    value = """
                                    {
                                      "status": 404,
                                      "message": "Note not found with id: 507f1f77bcf86cd799439011",
                                      "timestamp": "2024-11-09T14:30:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 500,
                                      "message": "An unexpected error occurred: Database read error",
                                      "timestamp": "2024-11-09T14:30:00"
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<NoteDetailResponse> getNoteById(
            @Parameter(
                    description = "MongoDB ObjectId of the note to retrieve",
                    required = true,
                    example = "507f1f77bcf86cd799439011"
            )
            @PathVariable String id) {
        NoteDetailResponse response = noteService.getNoteById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/stats")
    @Operation(
            summary = "Get word frequency statistics",
            description = "Analyzes the note's text content and returns word frequency statistics. " +
                    "Words are counted case-insensitively and sorted by frequency in descending order. " +
                    "Only alphabetic characters (including Cyrillic) are counted as words. " +
                    "Special characters, numbers, and punctuation are ignored."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistics calculated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Word statistics",
                                            description = "Words sorted by frequency (descending)",
                                            value = """
                                            {
                                              "project": 5,
                                              "meeting": 3,
                                              "budget": 2,
                                              "timeline": 2,
                                              "team": 1,
                                              "allocation": 1
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Empty statistics",
                                            description = "When note contains no valid words",
                                            value = """
                                            {}
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Cyrillic text statistics",
                                            value = """
                                            {
                                              "проект": 3,
                                              "зустріч": 2,
                                              "команда": 1,
                                              "бюджет": 1
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Note not found",
                                    value = """
                                    {
                                      "status": 404,
                                      "message": "Note not found with id: 507f1f77bcf86cd799439011",
                                      "timestamp": "2024-11-09T14:30:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 500,
                                      "message": "An unexpected error occurred: Text processing error",
                                      "timestamp": "2024-11-09T14:30:00"
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<Map<String, Long>> getWordStatistics(
            @Parameter(
                    description = "MongoDB ObjectId of the note to analyze",
                    required = true,
                    example = "507f1f77bcf86cd799439011"
            )
            @PathVariable String id) {
        Map<String, Long> stats = noteService.getWordStatistics(id);
        return ResponseEntity.ok(stats);
    }
}