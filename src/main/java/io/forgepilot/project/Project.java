package io.forgepilot.project;

import java.time.Instant;
import java.util.UUID;

/**
 * ForgePilot workspace project.
 *
 * @author Tejas Shah
 */
public record Project(
        UUID id,
        String name,
        String description,
        String stack,
        ProjectStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public enum ProjectStatus {
        DRAFT,
        PLANNING,
        BUILDING,
        READY,
        FAILED
    }
}
