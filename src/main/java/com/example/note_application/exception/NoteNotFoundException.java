package com.example.note_application.exception;

public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(String message) {
        super(message);
    }
}
