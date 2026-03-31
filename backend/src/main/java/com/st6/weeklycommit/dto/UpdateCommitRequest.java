package com.st6.weeklycommit.dto;

import com.st6.weeklycommit.entity.enums.Priority;

import java.util.UUID;

public record UpdateCommitRequest(
        String name,
        Priority priority,
        UUID goalId,
        String customGoalText
) {}
