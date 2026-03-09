package com.wealthwise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GoalControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUpUser() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "goaluser",
                      "password": "pass123",
                      "fullName": "Goal User",
                      "email":    "goal@test.com"
                    }
                    """));

        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "goaluser",
                      "password": "pass123"
                    }
                    """))
            .andReturn();

        token = objectMapper.readTree(result.getResponse().getContentAsString())
                            .get("token").asText();
    }

    // ── GET /api/goals ────────────────────────────────────────────────────────

    @Test
    void getAll_returns403WithoutToken() throws Exception {
        mvc.perform(get("/api/goals"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getAll_returns200WithValidToken() throws Exception {
        mvc.perform(get("/api/goals")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    // ── POST /api/goals ───────────────────────────────────────────────────────

    @Test
    void create_returns200WithCorrectData() throws Exception {
        mvc.perform(post("/api/goals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":                "Emergency Fund",
                      "targetAmount":        5000.00,
                      "monthlyContribution":  200.00,
                      "deadline":            "2027-01-01",
                      "icon":                "💰"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Emergency Fund"))
            .andExpect(jsonPath("$.targetAmount").value(5000.00))
            .andExpect(jsonPath("$.savedAmount").value(0.0))
            .andExpect(jsonPath("$.progressPercent").value(0.0));
    }

    @Test
    void create_savedAmountIsZeroByDefault() throws Exception {
        mvc.perform(post("/api/goals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":                "Vacation",
                      "targetAmount":        2000.00,
                      "monthlyContribution":  100.00,
                      "deadline":            "2026-12-01"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.savedAmount").value(0.0));
    }

    // ── PATCH /api/goals/{id}/deposit ─────────────────────────────────────────

    @Test
    void deposit_updatesGoalSavedAmount() throws Exception {
        // Create goal
        MvcResult created = mvc.perform(post("/api/goals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":                "Car",
                      "targetAmount":        10000.00,
                      "monthlyContribution":   500.00,
                      "deadline":            "2028-01-01"
                    }
                    """))
            .andReturn();

        Long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        // Deposit
        mvc.perform(patch("/api/goals/" + id + "/deposit")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 1500.00}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.savedAmount").value(1500.00));
    }

    @Test
    void deposit_capsAtTargetAmount() throws Exception {
        MvcResult created = mvc.perform(post("/api/goals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":                "Laptop",
                      "targetAmount":        1000.00,
                      "monthlyContribution":  100.00,
                      "deadline":            "2026-12-01"
                    }
                    """))
            .andReturn();

        Long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        // Deposit more than target
        mvc.perform(patch("/api/goals/" + id + "/deposit")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 9999.00}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.savedAmount").value(1000.00))
            .andExpect(jsonPath("$.progressPercent").value(100.0));
    }

    // ── DELETE /api/goals/{id} ────────────────────────────────────────────────

    @Test
    void delete_returns200AndRemovesGoal() throws Exception {
        MvcResult created = mvc.perform(post("/api/goals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":                "Trip",
                      "targetAmount":        500.00,
                      "monthlyContribution":  50.00,
                      "deadline":            "2026-09-01"
                    }
                    """))
            .andReturn();

        Long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(delete("/api/goals/" + id)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        mvc.perform(get("/api/goals")
                .header("Authorization", "Bearer " + token))
            .andExpect(jsonPath("$.length()").value(0));
    }
}
