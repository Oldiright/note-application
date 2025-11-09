package com.example.note_application.repository;

import com.example.note_application.model.Note;
import com.example.note_application.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface NoteRepository extends MongoRepository<Note, String> {
    Page<Note> findAllByOrderByCreatedDateDesc(Pageable pageable);

    Page<Note> findByTagsContainingOrderByCreatedDateDesc(Tag tag, Pageable pageable);

    //in this case we can use Spring Data naming convention (current) or @Query(for better understanding)

    // @Query(value = "{}", sort = "{ createdDate: -1 }")
    // Page<Note> findAllNotes(Pageable pageable);

    // @Query(value = "{ tags: ?0 }", sort = "{ createdDate: -1 }")
    // Page<Note> findByTag(Tag tag, Pageable pageable);
}
