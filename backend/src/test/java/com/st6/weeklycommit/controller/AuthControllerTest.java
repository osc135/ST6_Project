package com.st6.weeklycommit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ALICE_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String BOB_ID = "b2c3d4e5-f6a7-8901-bcde-f12345678901";

    // -------------------------------------------------------
    // POST /api/auth/register
    // -------------------------------------------------------

    @Test
    void register_success_returnsNewUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Eve Test",
                                "email", "eve@st6.com",
                                "password", "pass1234"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Eve Test")))
                .andExpect(jsonPath("$.email", is("eve@st6.com")))
                .andExpect(jsonPath("$.role", is("EMPLOYEE")))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void register_withManagerRole_setsManagerRole() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "New Manager",
                                "email", "mgr@st6.com",
                                "password", "pass1234",
                                "role", "MANAGER"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("MANAGER")));
    }

    @Test
    void register_missingFields_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Eve Test",
                                "password", "pass1234"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shortPassword_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Eve Test",
                                "email", "eve@st6.com",
                                "password", "ab"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_duplicateEmail_returnsConflict() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Duplicate",
                                "email", "alice@st6.com",
                                "password", "pass1234"
                        ))))
                .andExpect(status().isConflict());
    }

    // -------------------------------------------------------
    // POST /api/auth/login
    // -------------------------------------------------------

    @Test
    void login_validCredentials_returnsUser() throws Exception {
        // Register a user first since seed users have empty passwords
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Login Test",
                        "email", "logintest@st6.com",
                        "password", "mypass"
                ))));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "logintest@st6.com",
                                "password", "mypass"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Login Test")))
                .andExpect(jsonPath("$.email", is("logintest@st6.com")));
    }

    @Test
    void login_wrongPassword_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Login Test",
                        "email", "logintest2@st6.com",
                        "password", "correct"
                ))));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "logintest2@st6.com",
                                "password", "wrong"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_nonExistentEmail_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "nobody@st6.com",
                                "password", "whatever"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------
    // POST /api/auth/invite
    // -------------------------------------------------------

    @Test
    void invite_newEmployee_createsUser() throws Exception {
        mockMvc.perform(post("/api/auth/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Frank New",
                                "email", "frank@st6.com",
                                "managerId", ALICE_ID
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Frank New")))
                .andExpect(jsonPath("$.email", is("frank@st6.com")))
                .andExpect(jsonPath("$.role", is("EMPLOYEE")))
                .andExpect(jsonPath("$.managerId", is(ALICE_ID)));
    }

    @Test
    void invite_existingUserWithoutTeam_assignsToManager() throws Exception {
        // Register a user with no manager
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Grace Solo",
                        "email", "grace@st6.com",
                        "password", "pass1234"
                ))));

        // Invite that existing user
        mockMvc.perform(post("/api/auth/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Grace Solo",
                                "email", "grace@st6.com",
                                "managerId", ALICE_ID
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managerId", is(ALICE_ID)));
    }

    @Test
    void invite_existingUserAlreadyOnTeam_returnsConflict() throws Exception {
        // Bob already has a managerId
        mockMvc.perform(post("/api/auth/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Bob Smith",
                                "email", "bob@st6.com",
                                "managerId", ALICE_ID
                        ))))
                .andExpect(status().isConflict());
    }

    @Test
    void invite_nonExistentManager_returnsNotFound() throws Exception {
        mockMvc.perform(post("/api/auth/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Nobody",
                                "email", "new@st6.com",
                                "managerId", "00000000-0000-0000-0000-000000000099"
                        ))))
                .andExpect(status().isNotFound());
    }

    @Test
    void invite_nonManagerRole_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Nobody",
                                "email", "new2@st6.com",
                                "managerId", BOB_ID
                        ))))
                .andExpect(status().isBadRequest());
    }
}
