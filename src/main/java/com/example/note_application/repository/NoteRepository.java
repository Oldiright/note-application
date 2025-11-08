package com.example.note_application.repository;

import com.example.note_application.model.Note;
import com.example.note_application.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Set;

public interface NoteRepository extends MongoRepository<Note, String> {
    Page<Note> findAllByOrderByCreatedDateDesc(Pageable pageable);

    Page<Note> findByTagsContainingOrderByCreatedDateDesc(Tag tag, Pageable pageable);
}
