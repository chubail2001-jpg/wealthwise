package com.wealthwise.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTest {

    @Autowired private MockMvc mvc;

    // ── /register ────────────────────────────────────────────────────────────

    @Test
    void register_returns200OnSuccess() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username":  "testuser",
                      "password":  "password123",
                      "fullName":  "Test User",
                      "email":     "test@example.com"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    void register_returns400WhenUsernameAlreadyExists() throws Exception {
        String body = """
            {
              "username":  "dupuser",
              "password":  "pass123",
              "fullName":  "Dup User",
              "email":     "dup@example.com"
            }
            """;
        // First registration
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());

        // Second registration with same username
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Username already taken"));
    }

    @Test
    void register_returns400WhenEmailAlreadyExists() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "user1",
                      "password": "pass1",
                      "fullName": "User One",
                      "email":    "shared@example.com"
                    }
                    """))
            .andExpect(status().isOk());

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "user2",
                      "password": "pass2",
                      "fullName": "User Two",
                      "email":    "shared@example.com"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    // ── /login ────────────────────────────────────────────────────────────────

    @Test
    void login_returns200WithTokenAndUser() throws Exception {
        // Register first
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "loginuser",
                      "password": "mypassword",
                      "fullName": "Login User",
                      "email":    "login@example.com"
                    }
                    """))
            .andExpect(status().isOk());

        // Then login
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "loginuser",
                      "password": "mypassword"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.user.username").value("loginuser"));
    }

    @Test
    void login_returns401WithWrongPassword() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "wrongpass",
                      "password": "correctpass",
                      "fullName": "Wrong Pass",
                      "email":    "wrong@example.com"
                    }
                    """))
            .andExpect(status().isOk());

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "wrongpass",
                      "password": "wrongpassword"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void login_returns401ForNonExistentUser() throws Exception {
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "ghost",
                      "password": "nopass"
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }
}
