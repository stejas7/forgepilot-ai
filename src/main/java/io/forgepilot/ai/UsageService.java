package io.forgepilot.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.platform.PlatformStateStore;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UsageService {
    private static final String STATE_FILE = "usage.json";
    private final PlatformStateStore stateStore;
    private final Map<UUID, Usage> usageByProject;

    public UsageService(PlatformStateStore stateStore) {
        this.stateStore = stateStore;
        this.usageByProject = new LinkedHashMap<>(stateStore.read(
                STATE_FILE,
                new TypeReference<Map<UUID, Usage>>() {},
                LinkedHashMap::new));
    }

    public synchronized Usage get(UUID projectId) {
        return usageByProject.getOrDefault(projectId, new Usage(0, 0, 0));
    }

    public synchronized Usage record(UUID projectId, int inputTokens, int outputTokens) {
        Usage current = get(projectId);
        Usage next = new Usage(
                current.requests() + 1,
                current.inputTokens() + Math.max(0, inputTokens),
                current.outputTokens() + Math.max(0, outputTokens));
        usageByProject.put(projectId, next);
        stateStore.write(STATE_FILE, usageByProject);
        return next;
    }

    public record Usage(long requests, long inputTokens, long outputTokens) {
        public long totalTokens() { return inputTokens + outputTokens; }
    }
}
