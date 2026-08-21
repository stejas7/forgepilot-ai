package io.forgepilot.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.platform.PlatformStateStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AttachmentService {
    private static final String STATE_FILE = "attachments.json";
    private final PlatformStateStore stateStore;
    private final Map<UUID, List<Attachment>> attachments;

    public AttachmentService(PlatformStateStore stateStore) {
        this.stateStore = stateStore;
        Map<UUID, List<Attachment>> loaded = stateStore.read(
                STATE_FILE,
                new TypeReference<Map<UUID, List<Attachment>>>() {},
                LinkedHashMap::new);
        this.attachments = new LinkedHashMap<>();
        loaded.forEach((id, values) -> this.attachments.put(id, new ArrayList<>(values)));
    }

    public synchronized List<Attachment> list(UUID projectId) {
        return List.copyOf(attachments.computeIfAbsent(projectId, ignored -> new ArrayList<>()));
    }

    public synchronized Attachment add(UUID projectId, MultipartFile file) {
        try {
            String mime = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
            Attachment attachment = new Attachment(
                    UUID.randomUUID(), projectId,
                    sanitize(file.getOriginalFilename()), mime,
                    Base64.getEncoder().encodeToString(file.getBytes()),
                    file.getSize(), Instant.now());
            attachments.computeIfAbsent(projectId, ignored -> new ArrayList<>()).add(attachment);
            stateStore.write(STATE_FILE, attachments);
            return attachment;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read attachment", exception);
        }
    }

    public synchronized void clear(UUID projectId) {
        attachments.remove(projectId);
        stateStore.write(STATE_FILE, attachments);
    }

    private String sanitize(String name) {
        if (name == null || name.isBlank()) return "attachment";
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public record Attachment(UUID id, UUID projectId, String name, String mimeType,
                             String base64, long size, Instant createdAt) {}
}
