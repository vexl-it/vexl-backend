package com.cleevio.vexl.module.stats.controller;

import com.cleevio.vexl.module.stats.service.StatsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Stats")
@RestController
@RequestMapping("/internal/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/process-stats")
    @ResponseStatus(HttpStatus.OK)
    public void processStats() {
        statsService.processStats();
    }
}
