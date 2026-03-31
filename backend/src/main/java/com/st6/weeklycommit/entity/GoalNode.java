package com.st6.weeklycommit.entity;

import com.st6.weeklycommit.entity.enums.GoalLevel;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "goal_nodes")
public class GoalNode {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalLevel level;

    @Column(name = "parent_id")
    private UUID parentId;

    public GoalNode() {}

    public GoalNode(UUID id, String title, GoalLevel level, UUID parentId) {
        this.id = id;
        this.title = title;
        this.level = level;
        this.parentId = parentId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public GoalLevel getLevel() { return level; }
    public void setLevel(GoalLevel level) { this.level = level; }

    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }
}
