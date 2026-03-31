package com.st6.weeklycommit.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReconcileRequest(
        @NotNull UUID commitId,
        boolean done,
        String explanation
) {}
