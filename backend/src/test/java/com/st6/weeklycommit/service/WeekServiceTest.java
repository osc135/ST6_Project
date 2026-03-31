package com.st6.weeklycommit.service;

import com.st6.weeklycommit.entity.Week;
import com.st6.weeklycommit.repository.WeekRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeekServiceTest {

    @Mock
    private WeekRepository weekRepository;

    @InjectMocks
    private WeekService service;

    @Test
    void getById_existingWeek_returnsWeek() {
        UUID id = UUID.randomUUID();
        Week week = new Week(id, LocalDate.of(2026, 3, 30), LocalDate.of(2026, 4, 3));
        when(weekRepository.findById(id)).thenReturn(Optional.of(week));

        Week result = service.getById(id);

        assertEquals(id, result.getId());
        assertEquals(LocalDate.of(2026, 3, 30), result.getStartDate());
    }

    @Test
    void getById_nonExistentWeek_throwsException() {
        UUID id = UUID.randomUUID();
        when(weekRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.getById(id));
    }

    @Test
    void getOrCreateWeekFor_existingWeek_returnsIt() {
        LocalDate date = LocalDate.of(2026, 3, 31);
        Week existing = new Week(UUID.randomUUID(), LocalDate.of(2026, 3, 30), LocalDate.of(2026, 4, 3));
        when(weekRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date))
                .thenReturn(Optional.of(existing));

        Week result = service.getOrCreateWeekFor(date);

        assertEquals(existing.getId(), result.getId());
    }

    @Test
    void getOrCreateWeekFor_noExistingWeek_createsNewOne() {
        LocalDate date = LocalDate.of(2026, 4, 7); // a Tuesday
        when(weekRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date))
                .thenReturn(Optional.empty());
        when(weekRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Week result = service.getOrCreateWeekFor(date);

        assertEquals(LocalDate.of(2026, 4, 6), result.getStartDate()); // Monday
        assertEquals(LocalDate.of(2026, 4, 10), result.getEndDate()); // Friday
    }
}
