package com.st6.weeklycommit.controller;

import com.st6.weeklycommit.entity.Week;
import com.st6.weeklycommit.service.WeekService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/weeks")
public class WeekController {

    private final WeekService weekService;

    public WeekController(WeekService weekService) {
        this.weekService = weekService;
    }

    @GetMapping("/current")
    public Week getCurrentWeek() {
        return weekService.getCurrentWeek();
    }

    @GetMapping("/prior")
    public Week getPriorWeek() {
        return weekService.getPriorWeek();
    }

    @GetMapping("/by-date")
    public Week getByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return weekService.getOrCreateWeekFor(date);
    }

    @GetMapping("/{id}")
    public Week getById(@PathVariable UUID id) {
        return weekService.getById(id);
    }
}
