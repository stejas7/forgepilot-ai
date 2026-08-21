package io.forgepilot.project;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.platform.PlatformStateStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Durable ForgePilot project service.
 *
 * @author Tejas Shah
 */
@Service
public class ProjectService {

    private static final String STATE_FILE = "projects.json";

    private final PlatformStateStore stateStore;
    private final Map<UUID, Project> projects;

    public ProjectService(PlatformStateStore stateStore) {
        this.stateStore = stateStore;
        this.projects = new LinkedHashMap<>(stateStore.read(
                STATE_FILE,
                new TypeReference<Map<UUID, Project>>() {},
                LinkedHashMap::new));
    }

    public synchronized List<Project> findAll() {
        return projects.values().stream()
                .sorted(Comparator.comparing(Project::updatedAt).reversed())
                .toList();
    }

    public synchronized Project findById(UUID id) {
        Project project = projects.get(id);
        if (project == null) {
            throw new ProjectNotFoundException(id);
        }
        return project;
    }

    public synchronized Project create(String name, String description, String stack) {
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
        persist();
        return project;
    }

    public synchronized Project updateStatus(UUID id, Project.ProjectStatus status) {
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
        persist();
        return updated;
    }

    private void persist() {
        stateStore.write(STATE_FILE, projects);
    }

    static class ProjectNotFoundException extends RuntimeException {
        ProjectNotFoundException(UUID id) {
            super("Project not found: " + id);
        }
    }
}
