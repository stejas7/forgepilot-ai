package io.forgepilot.backend;

import io.forgepilot.workspace.WorkspaceService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Generates a minimal Spring Boot CRUD slice directly into the ForgePilot workspace. */
@Service
public class BackendCodeGeneratorService {
    private final WorkspaceService workspaceService;

    public BackendCodeGeneratorService(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    public GenerationResult generate(UUID projectId, String resourceName, String packageName) {
        String entity = normalizeType(resourceName);
        String variable = Character.toLowerCase(entity.charAt(0)) + entity.substring(1);
        String pkg = normalizePackage(packageName);
        String base = "backend/src/main/java/" + pkg.replace('.', '/') + "/";
        Map<String, String> files = new LinkedHashMap<>();

        files.put(base + entity + ".java", entitySource(pkg, entity));
        files.put(base + entity + "Repository.java", repositorySource(pkg, entity));
        files.put(base + entity + "Service.java", serviceSource(pkg, entity, variable));
        files.put(base + entity + "Controller.java", controllerSource(pkg, entity, variable));
        files.put("backend/src/main/resources/application-generated.yml", "spring:\n  datasource:\n    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/forgepilot}\n  jpa:\n    hibernate:\n      ddl-auto: validate\n");

        files.forEach((path, content) -> workspaceService.putFile(projectId, path, content));
        workspaceService.snapshot(projectId, "P63 backend generator: " + entity);
        return new GenerationResult(entity, pkg, Map.copyOf(files));
    }

    private String normalizeType(String value) {
        String cleaned = value == null ? "Resource" : value.replaceAll("[^A-Za-z0-9 ]", " ").trim();
        if (cleaned.isBlank()) return "Resource";
        StringBuilder out = new StringBuilder();
        for (String part : cleaned.split("\\s+")) {
            if (!part.isBlank()) out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        if (out.isEmpty()) return "Resource";
        if (Character.isDigit(out.charAt(0))) out.insert(0, "Resource");
        return out.toString();
    }

    private String normalizePackage(String value) {
        String pkg = value == null || value.isBlank() ? "com.forgepilot.generated" : value.trim().toLowerCase();
        pkg = pkg.replaceAll("[^a-z0-9_.]", "").replaceAll("\\.{2,}", ".");
        if (pkg.startsWith(".")) pkg = pkg.substring(1);
        if (pkg.endsWith(".")) pkg = pkg.substring(0, pkg.length() - 1);
        return pkg.isBlank() ? "com.forgepilot.generated" : pkg;
    }

    private String entitySource(String pkg, String entity) {
        return "package " + pkg + ";\n\nimport jakarta.persistence.*;\nimport java.util.UUID;\n\n@Entity\n@Table(name = \"" + snake(entity) + "\")\npublic class " + entity + " {\n    @Id @GeneratedValue private UUID id;\n    @Column(nullable = false) private String name;\n    public UUID getId(){ return id; }\n    public String getName(){ return name; }\n    public void setName(String name){ this.name = name; }\n}\n";
    }

    private String repositorySource(String pkg, String entity) {
        return "package " + pkg + ";\n\nimport org.springframework.data.jpa.repository.JpaRepository;\nimport java.util.UUID;\n\npublic interface " + entity + "Repository extends JpaRepository<" + entity + ", UUID> {}\n";
    }

    private String serviceSource(String pkg, String entity, String variable) {
        return "package " + pkg + ";\n\nimport org.springframework.stereotype.Service;\nimport java.util.List;\nimport java.util.UUID;\n\n@Service\npublic class " + entity + "Service {\n    private final " + entity + "Repository repository;\n    public " + entity + "Service(" + entity + "Repository repository){ this.repository = repository; }\n    public List<" + entity + "> list(){ return repository.findAll(); }\n    public " + entity + " create(" + entity + " " + variable + "){ return repository.save(" + variable + "); }\n    public void delete(UUID id){ repository.deleteById(id); }\n}\n";
    }

    private String controllerSource(String pkg, String entity, String variable) {
        return "package " + pkg + ";\n\nimport org.springframework.web.bind.annotation.*;\nimport java.util.List;\nimport java.util.UUID;\n\n@RestController\n@RequestMapping(\"/api/generated/" + snake(entity).replace('_', '-') + "\")\npublic class " + entity + "Controller {\n    private final " + entity + "Service service;\n    public " + entity + "Controller(" + entity + "Service service){ this.service = service; }\n    @GetMapping public List<" + entity + "> list(){ return service.list(); }\n    @PostMapping public " + entity + " create(@RequestBody " + entity + " " + variable + "){ return service.create(" + variable + "); }\n    @DeleteMapping(\"/{id}\") public void delete(@PathVariable UUID id){ service.delete(id); }\n}\n";
    }

    private String snake(String value) {
        return value.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }

    public record GenerationResult(String resourceName, String packageName, Map<String, String> files) {}
}
