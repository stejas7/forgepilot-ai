package io.forgepilot.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.platform.PlatformStateStore;
import io.forgepilot.workspace.WorkspaceService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Project-scoped generated backend contract. Metadata is durable today and is
 * deliberately separated from the storage adapter so PostgreSQL can back the
 * same API without changing the builder.
 */
@Service
public class GeneratedBackendService {
    private static final String STATE = "generated-backends.json";
    private final PlatformStateStore stateStore;
    private final WorkspaceService workspaceService;
    private final Map<UUID, BackendProject> backends;

    public GeneratedBackendService(PlatformStateStore stateStore, WorkspaceService workspaceService) {
        this.stateStore = stateStore;
        this.workspaceService = workspaceService;
        this.backends = new LinkedHashMap<>(stateStore.read(STATE,
                new TypeReference<Map<UUID, BackendProject>>() {}, LinkedHashMap::new));
    }

    public synchronized BackendProject get(UUID projectId) {
        return backends.computeIfAbsent(projectId, this::defaultBackend);
    }

    public synchronized BackendProject provision(UUID projectId) {
        BackendProject current = get(projectId);
        BackendProject provisioned = new BackendProject(projectId, "READY", current.auth(), current.tables(), current.secrets(), current.logs(), Instant.now());
        backends.put(projectId, provisioned);
        workspaceService.putFile(projectId, "backend/config.json", "{\n  \"auth\": true,\n  \"storage\": true,\n  \"realtime\": false\n}\n");
        if (workspaceService.listFiles(projectId).stream().noneMatch(f -> f.path().equals("db/schema.sql"))) {
            workspaceService.putFile(projectId, "db/schema.sql", "create table app_user (id uuid primary key, email varchar(200) unique not null, created_at timestamp not null);\n");
        }
        log(projectId, "Backend provisioned");
        persist();
        return get(projectId);
    }

    public synchronized BackendProject configureAuth(UUID projectId, boolean emailPassword, boolean oauth, boolean rbac) {
        BackendProject current = get(projectId);
        AuthConfig auth = new AuthConfig(emailPassword, oauth, rbac);
        BackendProject updated = new BackendProject(projectId, current.status(), auth, current.tables(), current.secrets(), current.logs(), Instant.now());
        backends.put(projectId, updated); log(projectId, "Authentication configuration updated"); persist(); return get(projectId);
    }

    public synchronized BackendProject putSecret(UUID projectId, String name, String value) {
        BackendProject current = get(projectId);
        Map<String, String> secrets = new LinkedHashMap<>(current.secrets());
        secrets.put(name, mask(value));
        backends.put(projectId, new BackendProject(projectId, current.status(), current.auth(), current.tables(), Map.copyOf(secrets), current.logs(), Instant.now()));
        log(projectId, "Secret updated: " + name); persist(); return get(projectId);
    }

    public synchronized BackendProject registerTable(UUID projectId, String name, List<Column> columns) {
        BackendProject current = get(projectId);
        List<TableDefinition> tables = new ArrayList<>(current.tables());
        tables.removeIf(t -> t.name().equalsIgnoreCase(name));
        tables.add(new TableDefinition(name, List.copyOf(columns), 0));
        backends.put(projectId, new BackendProject(projectId, current.status(), current.auth(), List.copyOf(tables), current.secrets(), current.logs(), Instant.now()));
        StringBuilder sql = new StringBuilder("create table ").append(name).append(" (\n");
        for (int i = 0; i < columns.size(); i++) {
            Column c = columns.get(i); sql.append("  ").append(c.name()).append(' ').append(c.type()); if (!c.nullable()) sql.append(" not null"); if (i < columns.size() - 1) sql.append(','); sql.append('\n');
        }
        sql.append(");\n");
        workspaceService.putFile(projectId, "db/" + name + ".sql", sql.toString());
        workspaceService.snapshot(projectId, "Backend schema: " + name);
        log(projectId, "Table registered: " + name); persist(); return get(projectId);
    }

    public synchronized List<String> logs(UUID projectId) { return get(projectId).logs(); }

    private BackendProject defaultBackend(UUID projectId) {
        return new BackendProject(projectId, "NOT_PROVISIONED", new AuthConfig(false, false, false), List.of(), Map.of(), List.of(), Instant.now());
    }

    private void log(UUID projectId, String message) {
        BackendProject current = get(projectId);
        List<String> logs = new ArrayList<>(current.logs()); logs.add(Instant.now() + " " + message);
        backends.put(projectId, new BackendProject(projectId, current.status(), current.auth(), current.tables(), current.secrets(), List.copyOf(logs), Instant.now()));
    }
    private void persist() { stateStore.write(STATE, backends); }
    private String mask(String value) { if (value == null || value.isBlank()) return ""; return "••••" + value.substring(Math.max(0, value.length() - Math.min(4, value.length()))); }

    public record AuthConfig(boolean emailPassword, boolean oauth, boolean rbac) {}
    public record Column(String name, String type, boolean nullable) {}
    public record TableDefinition(String name, List<Column> columns, long rowCount) {}
    public record BackendProject(UUID projectId, String status, AuthConfig auth, List<TableDefinition> tables, Map<String,String> secrets, List<String> logs, Instant updatedAt) {}
}
