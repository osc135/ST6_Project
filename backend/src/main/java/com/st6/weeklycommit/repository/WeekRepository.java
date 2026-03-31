package com.st6.weeklycommit.repository;

import com.st6.weeklycommit.entity.Week;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface WeekRepository extends JpaRepository<Week, UUID> {
    Optional<Week> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate date1, LocalDate date2);
}
