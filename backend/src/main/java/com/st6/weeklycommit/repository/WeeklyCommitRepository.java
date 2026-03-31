package com.st6.weeklycommit.repository;

import com.st6.weeklycommit.entity.WeeklyCommit;
import com.st6.weeklycommit.entity.enums.CommitStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WeeklyCommitRepository extends JpaRepository<WeeklyCommit, UUID> {
    List<WeeklyCommit> findByOwnerIdAndWeekId(UUID ownerId, UUID weekId);
    List<WeeklyCommit> findByWeekId(UUID weekId);
    List<WeeklyCommit> findByOwnerIdAndWeekIdAndCarriedOverFromIsNotNull(UUID ownerId, UUID weekId);
    List<WeeklyCommit> findByOwnerIdAndWeekIdAndCarriedOverFromIsNull(UUID ownerId, UUID weekId);
    List<WeeklyCommit> findByOwnerIdAndWeekIdAndStatus(UUID ownerId, UUID weekId, CommitStatus status);
}
