package com.taska.domain.stats;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * Returns an aggregated statistics snapshot including task counts, completion streak,
     * estimated remaining time, and per-project and per-day breakdowns.
     *
     * @return the current stats DTO
     */
    @GetMapping("/overview")
    public StatsDto overview() {
        return statsService.compute();
    }
}
