package io.forgepilot.backend;

import io.forgepilot.workspace.WorkspaceService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Generates project-scoped authentication scaffolding into the ForgePilot workspace. */
@Service
public class AuthenticationScaffoldService {
    private final WorkspaceService workspaceService;

    public AuthenticationScaffoldService(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    public AuthScaffoldResult generate(UUID projectId, AuthScaffoldRequest request) {
        String packageName = sanitizePackage(request.packageName());
        List<String> roles = request.roles() == null || request.roles().isEmpty()
                ? List.of("USER", "ADMIN")
                : request.roles().stream().map(this::sanitizeRole).distinct().toList();
        Map<String, String> files = new LinkedHashMap<>();
        String javaBase = "generated/backend/src/main/java/" + packageName.replace('.', '/') + "/auth/";

        files.put(javaBase + "AppRole.java", enumFile(packageName, roles));
        files.put(javaBase + "AuthUser.java", userFile(packageName));
        files.put(javaBase + "AuthSession.java", sessionFile(packageName));
        files.put(javaBase + "GeneratedSecurityConfig.java", securityFile(packageName, roles, request.oauthEnabled()));
        files.put(javaBase + "AuthController.java", controllerFile(packageName));
        files.put("generated/frontend/src/auth/ProtectedRoute.tsx", protectedRouteFile());
        files.put("generated/frontend/src/auth/session.ts", sessionClientFile());
        files.put("generated/auth/auth-manifest.json", manifest(packageName, roles, request));

        files.forEach((path, content) -> workspaceService.putFile(projectId, path, content));
        workspaceService.snapshot(projectId, "P65 authentication scaffold");
        return new AuthScaffoldResult(packageName, roles, request.oauthEnabled(), request.emailPasswordEnabled(), Map.copyOf(files));
    }

    private String sanitizePackage(String value) {
        String raw = value == null || value.isBlank() ? "com.forgepilot.generated" : value.trim().toLowerCase();
        String cleaned = raw.replaceAll("[^a-z0-9_.]", "").replaceAll("\\.{2,}", ".");
        return cleaned.isBlank() ? "com.forgepilot.generated" : cleaned;
    }

    private String sanitizeRole(String role) {
        String cleaned = role == null ? "USER" : role.trim().toUpperCase().replaceAll("[^A-Z0-9_]", "_");
        return cleaned.isBlank() ? "USER" : cleaned;
    }

    private String enumFile(String pkg, List<String> roles) {
        return "package " + pkg + ".auth;\n\npublic enum AppRole { " + String.join(", ", roles) + " }\n";
    }

    private String userFile(String pkg) {
        return "package " + pkg + ".auth;\n\nimport java.util.Set;\nimport java.util.UUID;\n\npublic record AuthUser(UUID id, String email, Set<AppRole> roles, boolean enabled) {}\n";
    }

    private String sessionFile(String pkg) {
        return "package " + pkg + ".auth;\n\nimport java.time.Instant;\nimport java.util.UUID;\n\npublic record AuthSession(UUID userId, String token, Instant expiresAt) {\n    public boolean expired() { return expiresAt == null || expiresAt.isBefore(Instant.now()); }\n}\n";
    }

    private String securityFile(String pkg, List<String> roles, boolean oauth) {
        String authority = roles.get(0);
        return "package " + pkg + ".auth;\n\nimport org.springframework.context.annotation.Bean;\nimport org.springframework.context.annotation.Configuration;\nimport org.springframework.security.config.annotation.web.builders.HttpSecurity;\nimport org.springframework.security.web.SecurityFilterChain;\n\n@Configuration\npublic class GeneratedSecurityConfig {\n    @Bean SecurityFilterChain generatedSecurity(HttpSecurity http) throws Exception {\n        http.authorizeHttpRequests(auth -> auth\n            .requestMatchers(\"/api/public/**\", \"/api/auth/session\").permitAll()\n            .requestMatchers(\"/api/admin/**\").hasRole(\"" + authority + "\")\n            .anyRequest().authenticated())\n            .formLogin(form -> form.disable());\n        " + (oauth ? "http.oauth2Login(oauth2 -> {});" : "") + "\n        return http.build();\n    }\n}\n";
    }

    private String controllerFile(String pkg) {
        return "package " + pkg + ".auth;\n\nimport org.springframework.web.bind.annotation.GetMapping;\nimport org.springframework.web.bind.annotation.RequestMapping;\nimport org.springframework.web.bind.annotation.RestController;\n\nimport java.security.Principal;\nimport java.util.Map;\n\n@RestController\n@RequestMapping(\"/api/auth\")\npublic class AuthController {\n    @GetMapping(\"/session\")\n    public Map<String,Object> session(Principal principal) {\n        return Map.of(\"authenticated\", principal != null, \"name\", principal == null ? \"\" : principal.getName());\n    }\n}\n";
    }

    private String protectedRouteFile() {
        return "import {Navigate} from 'react-router-dom'\nimport type {ReactNode} from 'react'\nimport {useSession} from './session'\n\nexport function ProtectedRoute({children,roles=[]}:{children:ReactNode;roles?:string[]}){\n  const {loading,user}=useSession()\n  if(loading)return <div>Loading…</div>\n  if(!user)return <Navigate to=\"/login\" replace/>\n  if(roles.length&&!roles.some(role=>user.roles.includes(role)))return <Navigate to=\"/forbidden\" replace/>\n  return <>{children}</>\n}\n";
    }

    private String sessionClientFile() {
        return "import {useEffect,useState} from 'react'\n\nexport type SessionUser={name:string;roles:string[]}\nexport function useSession(){\n const [user,setUser]=useState<SessionUser|null>(null),[loading,setLoading]=useState(true)\n useEffect(()=>{fetch('/api/auth/session').then(r=>r.json()).then(v=>setUser(v.authenticated?{name:v.name,roles:v.roles??[]}:null)).finally(()=>setLoading(false))},[])\n return {user,loading}\n}\n";
    }

    private String manifest(String pkg, List<String> roles, AuthScaffoldRequest request) {
        return "{\n  \"package\": \"" + pkg + "\",\n  \"roles\": [\"" + String.join("\", \"", roles) + "\"],\n  \"emailPassword\": " + request.emailPasswordEnabled() + ",\n  \"oauth\": " + request.oauthEnabled() + ",\n  \"protectedRoutes\": true,\n  \"sessionPattern\": \"server-session\"\n}\n";
    }

    public record AuthScaffoldRequest(String packageName, List<String> roles, boolean emailPasswordEnabled, boolean oauthEnabled) {}
    public record AuthScaffoldResult(String packageName, List<String> roles, boolean oauthEnabled, boolean emailPasswordEnabled, Map<String,String> files) {}
}
