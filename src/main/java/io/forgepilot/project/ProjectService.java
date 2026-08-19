package io.forgepilot.project;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory project workspace service for the first ForgePilot product slice.
 * Persistence will move to PostgreSQL in the backend-platform milestone.
 *
 * @author Tejas Shah
 */
@Service
public class ProjectService {

    private final Map<UUID, Project> projects = new ConcurrentHashMap<>();

    public List<Project> findAll() {
        return projects.values().stream()
                .sorted(Comparator.comparing(Project::updatedAt).reversed())
                .toList();
    }

    public Project findById(UUID id) {
        Project project = projects.get(id);
        if (project == null) {
            throw new ProjectNotFoundException(id);
        }
        return project;
    }

    public Project create(String name, String description, String stack) {
        Instant now = Instant.now();
        Project project = new Project(
                UUID.randomUUID(),
                name.trim(),
                description == null ? "" : description.trim(),
                stack == null || stack.isBlank() ? "React + Spring Boot" : stack.trim(),
                Project.ProjectStatus.DRAFT,
                now,
                now);
        projects.put(project.id(), project);
        return project;
    }

    public Project updateStatus(UUID id, Project.ProjectStatus status) {
        Project current = findById(id);
        Project updated = new Project(
                current.id(),
                current.name(),
                current.description(),
                current.stack(),
                status,
                current.createdAt(),
                Instant.now());
        projects.put(id, updated);
        return updated;
    }

    static class ProjectNotFoundException extends RuntimeException {
        ProjectNotFoundException(UUID id) {
            super("Project not found: " + id);
        }
    }
}
