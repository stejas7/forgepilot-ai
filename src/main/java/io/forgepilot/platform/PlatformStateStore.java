package io.forgepilot.platform;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Supplier;

/**
 * Small durable JSON store for ForgePilot platform metadata.
 *
 * The creator product needs projects, conversation and workspace history to
 * survive container restarts before generated-app PostgreSQL arrives in P6.
 * Files are written atomically inside the configured persistent data volume.
 *
 * @author Tejas Shah
 */
@Service
public class PlatformStateStore {

    private final ObjectMapper objectMapper;
    private final Path root;

    public PlatformStateStore(ObjectMapper objectMapper,
                              @Value("${forgepilot.data-dir:/var/lib/forgepilot}") String dataDir) {
        this.objectMapper = objectMapper;
        this.root = Path.of(dataDir);
        try {
            Files.createDirectories(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create ForgePilot data directory", exception);
        }
    }

    public synchronized <T> T read(String fileName, TypeReference<T> type, Supplier<T> fallback) {
        Path file = root.resolve(fileName);
        if (!Files.exists(file)) {
            return fallback.get();
        }
        try {
            return objectMapper.readValue(file.toFile(), type);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read ForgePilot state: " + fileName, exception);
        }
    }

    public synchronized void write(String fileName, Object value) {
        Path target = root.resolve(fileName);
        Path temp = root.resolve(fileName + ".tmp");
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(temp.toFile(), value);
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to persist ForgePilot state: " + fileName, exception);
        }
    }
}
