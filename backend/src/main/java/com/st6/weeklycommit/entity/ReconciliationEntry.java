package com.st6.weeklycommit.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_entries")
public class ReconciliationEntry {

    @Id
    private UUID id;

    @Column(name = "commit_id", nullable = false)
    private UUID commitId;

    @Column(nullable = false)
    private boolean done;

    private String explanation;

    public ReconciliationEntry() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCommitId() { return commitId; }
    public void setCommitId(UUID commitId) { this.commitId = commitId; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
