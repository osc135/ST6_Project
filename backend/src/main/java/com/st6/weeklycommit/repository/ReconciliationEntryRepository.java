package com.st6.weeklycommit.repository;

import com.st6.weeklycommit.entity.ReconciliationEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReconciliationEntryRepository extends JpaRepository<ReconciliationEntry, UUID> {
    Optional<ReconciliationEntry> findByCommitId(UUID commitId);
}
