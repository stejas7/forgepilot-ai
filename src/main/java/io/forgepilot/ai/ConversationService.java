package io.forgepilot.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.platform.PlatformStateStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Durable project-scoped AI conversation history.
 *
 * @author Tejas Shah
 */
@Service
public class ConversationService {

    private static final String STATE_FILE = "conversations.json";

    private final PlatformStateStore stateStore;
    private final Map<UUID, List<ConversationMessage>> messagesByProject;

    public ConversationService(PlatformStateStore stateStore) {
        this.stateStore = stateStore;
        Map<UUID, List<ConversationMessage>> loaded = stateStore.read(
                STATE_FILE,
                new TypeReference<Map<UUID, List<ConversationMessage>>>() {},
                LinkedHashMap::new);
        this.messagesByProject = new LinkedHashMap<>();
        loaded.forEach((projectId, messages) ->
                this.messagesByProject.put(projectId, new ArrayList<>(messages)));
    }

    public synchronized List<ConversationMessage> messages(UUID projectId) {
        return List.copyOf(messagesByProject.computeIfAbsent(projectId, ignored -> new ArrayList<>()));
    }

    public synchronized ConversationMessage addUser(UUID projectId, String mode, String content) {
        return add(projectId, "USER", mode, content);
    }

    public synchronized ConversationMessage addAssistant(UUID projectId, String mode, String content) {
        return add(projectId, "ASSISTANT", mode, content);
    }

    private ConversationMessage add(UUID projectId, String role, String mode, String content) {
        ConversationMessage message = new ConversationMessage(
                UUID.randomUUID(),
                projectId,
                role,
                mode,
                content == null ? "" : content,
                Instant.now());
        messagesByProject.computeIfAbsent(projectId, ignored -> new ArrayList<>()).add(message);
        stateStore.write(STATE_FILE, messagesByProject);
        return message;
    }

    public record ConversationMessage(
            UUID id,
            UUID projectId,
            String role,
            String mode,
            String content,
            Instant createdAt) {
    }
}
