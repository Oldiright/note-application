package com.example.noteapplication;

import com.example.noteapplication.dto.NoteCreateRequest;
import com.example.noteapplication.dto.NoteDetailResponse;
import com.example.noteapplication.dto.NoteUpdateRequest;
import com.example.noteapplication.model.Note;
import com.example.noteapplication.model.Tag;
import com.example.noteapplication.repository.NoteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NoteIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        // Додайте для дебагу
        System.out.println("=== TESTCONTAINERS MongoDB URI: " + mongoDBContainer.getReplicaSetUrl());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoteRepository noteRepository;

    private String createdNoteId;

    @BeforeEach
    void setUp() {
        noteRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Should create note with all fields")
    void shouldCreateNoteSuccessfully() throws Exception {
        NoteCreateRequest request = new NoteCreateRequest(
                "Project Planning Meeting",
                "Discuss project milestones, assign tasks to team members, and set deadlines for Q1 deliverables. " +
                        "Review budget allocation and resource requirements.",
                Set.of(Tag.BUSINESS, Tag.IMPORTANT)
        );

        MvcResult result = mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Project Planning Meeting"))
                .andExpect(jsonPath("$.text").value(containsString("project milestones")))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.createdDate").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        NoteDetailResponse response = objectMapper.readValue(responseBody, NoteDetailResponse.class);
        createdNoteId = response.id();

        Assertions.assertNotNull(createdNoteId);
        Assertions.assertEquals(1, noteRepository.count());
    }



}