package com.wealthwise.controller;

import com.wealthwise.dto.InsightResponse;
import com.wealthwise.service.InsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
public class InsightController {

    @Autowired private InsightService insightService;

    @GetMapping
    public ResponseEntity<InsightResponse> getInsights() {
        return ResponseEntity.ok(insightService.getInsights());
    }
}
