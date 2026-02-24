package com.wealthwise.controller;

import com.wealthwise.dto.ForecastResponse;
import com.wealthwise.service.ForecastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forecast")
public class ForecastController {

    @Autowired private ForecastService forecastService;

    @GetMapping
    public ResponseEntity<ForecastResponse> getForecast() {
        return ResponseEntity.ok(forecastService.getForecast());
    }
}
