package io.forgepilot.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.platform.PlatformStateStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PlanService {
    private static final String STATE_FILE = "plans.json";
    private final PlatformStateStore stateStore;
    private final Map<UUID, ProjectPlan> plans;

    public PlanService(PlatformStateStore stateStore) {
        this.stateStore = stateStore;
        this.plans = new LinkedHashMap<>(stateStore.read(
                STATE_FILE,
                new TypeReference<Map<UUID, ProjectPlan>>() {},
                LinkedHashMap::new));
    }

    public synchronized ProjectPlan get(UUID projectId) {
        return plans.get(projectId);
    }

    public synchronized ProjectPlan saveDraft(UUID projectId, String content) {
        ProjectPlan plan = new ProjectPlan(projectId, content == null ? "" : content, "DRAFT", Instant.now());
        plans.put(projectId, plan);
        persist();
        return plan;
    }

    public synchronized ProjectPlan update(UUID projectId, String content) {
        ProjectPlan current = plans.get(projectId);
        String status = current == null ? "DRAFT" : current.status();
        ProjectPlan plan = new ProjectPlan(projectId, content == null ? "" : content, status, Instant.now());
        plans.put(projectId, plan);
        persist();
        return plan;
    }

    public synchronized ProjectPlan approve(UUID projectId) {
        ProjectPlan current = plans.get(projectId);
        if (current == null) throw new IllegalStateException("No plan exists for project " + projectId);
        ProjectPlan approved = new ProjectPlan(projectId, current.content(), "APPROVED", Instant.now());
        plans.put(projectId, approved);
        persist();
        return approved;
    }

    private void persist() { stateStore.write(STATE_FILE, plans); }

    public record ProjectPlan(UUID projectId, String content, String status, Instant updatedAt) {}
}
