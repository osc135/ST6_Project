package com.st6.weeklycommit.service;

import com.st6.weeklycommit.entity.Week;
import com.st6.weeklycommit.repository.WeekRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Service
public class WeekService {

    private final WeekRepository weekRepository;

    public WeekService(WeekRepository weekRepository) {
        this.weekRepository = weekRepository;
    }

    public Week getCurrentWeek() {
        LocalDate today = LocalDate.now();
        return weekRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today)
                .orElseGet(() -> createWeekFor(today));
    }

    public Week getOrCreateWeekFor(LocalDate date) {
        return weekRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date)
                .orElseGet(() -> createWeekFor(date));
    }

    public Week getPriorWeek() {
        Week current = getCurrentWeek();
        LocalDate priorMonday = current.getStartDate().minusDays(7);
        return weekRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(priorMonday, priorMonday)
                .orElse(null);
    }

    public Week getNextWeek() {
        Week current = getCurrentWeek();
        LocalDate nextMonday = current.getEndDate().plusDays(3);
        return getOrCreateWeekFor(nextMonday);
    }

    public Week getNextWeekAfter(Week week) {
        LocalDate nextMonday = week.getEndDate().plusDays(3);
        return getOrCreateWeekFor(nextMonday);
    }

    public Week getById(UUID id) {
        return weekRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Week not found: " + id));
    }

    private Week createWeekFor(LocalDate date) {
        LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate friday = monday.plusDays(4);
        Week week = new Week(UUID.randomUUID(), monday, friday);
        return weekRepository.save(week);
    }
}
