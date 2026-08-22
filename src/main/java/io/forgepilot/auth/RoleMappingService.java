package io.forgepilot.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class RoleMappingService {
    private final Set<String> adminGroups;
    private final Set<String> editorGroups;
    private final Set<String> adminEmails;

    public RoleMappingService(@Value("${forgepilot.auth.admin-groups:forgepilot-admins}") String adminGroups,
                              @Value("${forgepilot.auth.editor-groups:forgepilot-editors}") String editorGroups,
                              @Value("${forgepilot.auth.admin-emails:}") String adminEmails) {
        this.adminGroups = parse(adminGroups);
        this.editorGroups = parse(editorGroups);
        this.adminEmails = parse(adminEmails);
    }

    public List<String> roles(OAuth2User user) {
        String email = attribute(user, "email");
        if (!email.isBlank() && adminEmails.contains(normalize(email))) {
            return List.of("OWNER", "ADMIN", "EDITOR", "VIEWER");
        }
        Set<String> groups = extractGroups(user);
        if (groups.stream().anyMatch(adminGroups::contains)) return List.of("OWNER", "ADMIN", "EDITOR", "VIEWER");
        if (groups.stream().anyMatch(editorGroups::contains)) return List.of("EDITOR", "VIEWER");
        // ForgePilot is currently a creator-first single workspace. Authenticated members must
        // be able to create projects and run Plan/Build unless an explicit enterprise policy
        // later maps them to a read-only workspace role.
        return List.of("EDITOR", "VIEWER");
    }

    private String attribute(OAuth2User user, String key) {
        Object value = user.getAttributes().get(key);
        return value == null ? "" : value.toString().trim();
    }

    private Set<String> extractGroups(OAuth2User user) {
        Object value = user.getAttributes().get("groups");
        Set<String> groups = new LinkedHashSet<>();
        if (value instanceof Iterable<?> iterable) for (Object group : iterable) groups.add(normalize(group));
        if (value instanceof String string) groups.addAll(parse(string));
        return groups;
    }

    private Set<String> parse(String value) {
        Set<String> result = new LinkedHashSet<>();
        Arrays.stream(value.split(",")).map(this::normalize).filter(v -> !v.isBlank()).forEach(result::add);
        return result;
    }

    private String normalize(Object value) {
        return value == null ? "" : value.toString().trim().toLowerCase(Locale.ROOT);
    }
}
