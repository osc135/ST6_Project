package com.st6.weeklycommit.dto;

import java.util.UUID;

public record TeamMemberSummary(
        UUID userId,
        String name,
        int taskCount,
        String alignmentStatus
) {}
