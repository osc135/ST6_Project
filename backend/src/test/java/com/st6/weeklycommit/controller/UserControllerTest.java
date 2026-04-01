package com.st6.weeklycommit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String ALICE_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

    @Test
    void getAll_returnsAllSeededUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void getById_existingUser_returnsUser() throws Exception {
        mockMvc.perform(get("/api/users/" + ALICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Alice Johnson")))
                .andExpect(jsonPath("$.email", is("alice@st6.com")))
                .andExpect(jsonPath("$.role", is("MANAGER")));
    }

    @Test
    void getById_nonExistent_returns404() throws Exception {
        mockMvc.perform(get("/api/users/00000000-0000-0000-0000-000000000099"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmail_existingUser_returnsUser() throws Exception {
        mockMvc.perform(get("/api/users/email/alice@st6.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Alice Johnson")))
                .andExpect(jsonPath("$.id", is(ALICE_ID)));
    }

    @Test
    void getByEmail_nonExistent_returns404() throws Exception {
        mockMvc.perform(get("/api/users/email/nobody@st6.com"))
                .andExpect(status().isNotFound());
    }
}
