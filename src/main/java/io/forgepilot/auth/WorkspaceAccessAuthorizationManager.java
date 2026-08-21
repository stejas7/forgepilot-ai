package io.forgepilot.auth;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.function.Supplier;

@Component
public class WorkspaceAccessAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private final AuthPolicyProperties properties;

    public WorkspaceAccessAuthorizationManager(AuthPolicyProperties properties) {
        this.properties = properties;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        Authentication authentication = authenticationSupplier.get();
        if (authentication == null || !authentication.isAuthenticated()) return new AuthorizationDecision(false);
        String email = resolveEmail(authentication);
        if (email.isBlank()) return new AuthorizationDecision(properties.getApprovedDomains().isEmpty());
        if (properties.getAdminEmails().stream().anyMatch(a -> a.equalsIgnoreCase(email))) return new AuthorizationDecision(true);
        if (properties.getApprovedDomains().isEmpty()) return new AuthorizationDecision(true);
        int at = email.lastIndexOf('@');
        if (at < 0) return new AuthorizationDecision(false);
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        boolean allowed = properties.getApprovedDomains().stream().map(v -> v.toLowerCase(Locale.ROOT).trim()).anyMatch(domain::equals);
        return new AuthorizationDecision(allowed);
    }

    private String resolveEmail(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            Object email = oauth2User.getAttributes().get("email");
            if (email != null) return email.toString().trim();
            Object preferred = oauth2User.getAttributes().get("preferred_username");
            if (preferred != null && preferred.toString().contains("@")) return preferred.toString().trim();
        }
        String name = authentication.getName();
        return name != null && name.contains("@") ? name.trim() : "";
    }
}
