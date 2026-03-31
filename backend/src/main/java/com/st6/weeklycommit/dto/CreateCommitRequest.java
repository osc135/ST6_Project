package com.st6.weeklycommit.dto;

import com.st6.weeklycommit.entity.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCommitRequest(
        @NotBlank String name,
        @NotNull Priority priority,
        UUID goalId,
        String customGoalText,
        @NotNull UUID ownerId,
        @NotNull UUID weekId
) {}
