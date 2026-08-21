package io.forgepilot.ai;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Project-scoped AI conversation history.
 *
 * This is the conversation abstraction used by the creator-first builder. The
 * storage implementation will move to PostgreSQL with the platform persistence
 * milestone without changing the API contract.
 *
 * @author Tejas Shah
 */
@Service
public class ConversationService {

    private final Map<UUID, List<ConversationMessage>> messagesByProject = new ConcurrentHashMap<>();

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
