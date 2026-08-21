package io.forgepilot.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Small PostgreSQL adapter used by generated applications in P6.
 * Each project is isolated in its own PostgreSQL schema.
 */
@Service
public class GeneratedDataService {
    private static final Pattern IDENTIFIER = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
    private final String url;
    private final String user;
    private final String password;

    public GeneratedDataService(
            @Value("${forgepilot.generated-db.url:${FORGEPILOT_DB_URL:}}") String url,
            @Value("${forgepilot.generated-db.user:${FORGEPILOT_DB_USER:}}") String user,
            @Value("${forgepilot.generated-db.password:${FORGEPILOT_DB_PASSWORD:}}") String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public boolean configured() {
        return url != null && !url.isBlank();
    }

    public void ensureProjectSchema(UUID projectId) {
        requireConfigured();
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.execute("create schema if not exists " + schema(projectId));
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to provision project PostgreSQL schema", exception);
        }
    }

    public void createTable(UUID projectId, String table, List<GeneratedBackendService.Column> columns) {
        validateIdentifier(table);
        ensureProjectSchema(projectId);
        if (columns == null || columns.isEmpty()) throw new IllegalArgumentException("At least one column is required");
        StringBuilder sql = new StringBuilder("create table if not exists ")
                .append(schema(projectId)).append('.').append(table).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            GeneratedBackendService.Column column = columns.get(i);
            validateIdentifier(column.name());
            sql.append(column.name()).append(' ').append(normalizeType(column.type()));
            if (!column.nullable()) sql.append(" not null");
            if (i < columns.size() - 1) sql.append(',');
        }
        sql.append(')');
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.execute(sql.toString());
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to create generated table", exception);
        }
    }

    public List<Map<String, Object>> rows(UUID projectId, String table, int limit) {
        validateIdentifier(table);
        ensureProjectSchema(projectId);
        int safeLimit = Math.max(1, Math.min(limit, 200));
        String sql = "select * from " + schema(projectId) + "." + table + " limit " + safeLimit;
        try (Connection connection = connection(); Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            List<Map<String, Object>> rows = new ArrayList<>();
            ResultSetMetaData metadata = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= metadata.getColumnCount(); i++) row.put(metadata.getColumnLabel(i), rs.getObject(i));
                rows.add(row);
            }
            return rows;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to read generated table", exception);
        }
    }

    public long rowCount(UUID projectId, String table) {
        validateIdentifier(table);
        try (Connection connection = connection(); Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery("select count(*) from " + schema(projectId) + "." + table)) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException exception) {
            return 0;
        }
    }

    private Connection connection() throws SQLException { return DriverManager.getConnection(url, user, password); }
    private String schema(UUID projectId) { return "fp_" + projectId.toString().replace("-", ""); }
    private void requireConfigured() { if (!configured()) throw new IllegalStateException("Generated PostgreSQL is not configured"); }
    private void validateIdentifier(String value) { if (value == null || !IDENTIFIER.matcher(value).matches()) throw new IllegalArgumentException("Invalid SQL identifier: " + value); }
    private String normalizeType(String value) {
        String type = value == null ? "text" : value.trim().toLowerCase();
        return switch (type) {
            case "uuid", "text", "boolean", "date", "timestamp", "integer", "bigint", "numeric", "jsonb" -> type;
            case "varchar", "string" -> "varchar(255)";
            default -> throw new IllegalArgumentException("Unsupported generated column type: " + value);
        };
    }
}
