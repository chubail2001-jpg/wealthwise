package com.wealthwise.controller;

import com.wealthwise.repository.UserRepository;
import com.wealthwise.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUpUser() throws Exception {
        // Register + login to get a JWT
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "txuser",
                      "password": "pass123",
                      "fullName": "Tx User",
                      "email":    "tx@test.com"
                    }
                    """));

        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "txuser",
                      "password": "pass123"
                    }
                    """))
            .andReturn();

        String body = result.getResponse().getContentAsString();
        token = objectMapper.readTree(body).get("token").asText();
    }

    // ── GET /api/transactions ─────────────────────────────────────────────────

    @Test
    void getAll_returns403WithoutToken() throws Exception {
        mvc.perform(get("/api/transactions"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getAll_returns200WithValidToken() throws Exception {
        mvc.perform(get("/api/transactions")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getAll_returnsEmptyArrayInitially() throws Exception {
        mvc.perform(get("/api/transactions")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    // ── POST /api/transactions ────────────────────────────────────────────────

    @Test
    void create_returns200WithValidBody() throws Exception {
        mvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "type":        "INCOME",
                      "amount":      3000.00,
                      "description": "Monthly salary",
                      "category":    "Salary",
                      "date":        "2026-03-01"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("INCOME"))
            .andExpect(jsonPath("$.amount").value(3000.00));
    }

    @Test
    void create_returns403WithoutToken() throws Exception {
        mvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "type": "INCOME", "amount": 100,
                      "description": "test", "category": "test",
                      "date": "2026-03-01"
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    // ── PUT /api/transactions/{id} ────────────────────────────────────────────

    @Test
    void update_returns200WithUpdatedData() throws Exception {
        // Create a transaction first
        MvcResult created = mvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "type": "EXPENSE", "amount": 50.00,
                      "description": "Lunch", "category": "Food",
                      "date": "2026-03-01"
                    }
                    """))
            .andReturn();

        Long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        // Update it
        mvc.perform(put("/api/transactions/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "type": "EXPENSE", "amount": 75.00,
                      "description": "Dinner", "category": "Food",
                      "date": "2026-03-01"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.amount").value(75.00))
            .andExpect(jsonPath("$.description").value("Dinner"));
    }

    // ── DELETE /api/transactions/{id} ─────────────────────────────────────────

    @Test
    void delete_returns200AndRemovesTransaction() throws Exception {
        // Create
        MvcResult created = mvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "type": "EXPENSE", "amount": 30.00,
                      "description": "Coffee", "category": "Food",
                      "date": "2026-03-01"
                    }
                    """))
            .andReturn();

        Long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        // Delete
        mvc.perform(delete("/api/transactions/" + id)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        // Verify it's gone
        mvc.perform(get("/api/transactions")
                .header("Authorization", "Bearer " + token))
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void delete_returns403WithoutToken() throws Exception {
        mvc.perform(delete("/api/transactions/999"))
            .andExpect(status().isForbidden());
    }
}
