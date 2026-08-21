package io.forgepilot.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceAccessAuthorizationManagerTest {
    @Test
    void allowsApprovedDomain() {
        AuthPolicyProperties properties = new AuthPolicyProperties();
        properties.setApprovedDomains(List.of("example.com"));
        WorkspaceAccessAuthorizationManager manager = new WorkspaceAccessAuthorizationManager(properties);
        var user = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("OAUTH2_USER")), Map.of("sub","1","email","dev@example.com"), "sub");
        var auth = new UsernamePasswordAuthenticationToken(user, "n/a", user.getAuthorities());
        assertThat(manager.check(() -> auth, null).isGranted()).isTrue();
    }

    @Test
    void deniesUnapprovedDomain() {
        AuthPolicyProperties properties = new AuthPolicyProperties();
        properties.setApprovedDomains(List.of("example.com"));
        WorkspaceAccessAuthorizationManager manager = new WorkspaceAccessAuthorizationManager(properties);
        var user = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("OAUTH2_USER")), Map.of("sub","2","email","dev@other.com"), "sub");
        var auth = new UsernamePasswordAuthenticationToken(user, "n/a", user.getAuthorities());
        assertThat(manager.check(() -> auth, null).isGranted()).isFalse();
    }

    @Test
    void permitsEmergencyAdminRegardlessOfDomain() {
        AuthPolicyProperties properties = new AuthPolicyProperties();
        properties.setApprovedDomains(List.of("example.com"));
        properties.setAdminEmails(List.of("owner@recovery.dev"));
        WorkspaceAccessAuthorizationManager manager = new WorkspaceAccessAuthorizationManager(properties);
        var auth = new UsernamePasswordAuthenticationToken("owner@recovery.dev", "n/a", List.of(new SimpleGrantedAuthority("USER")));
        assertThat(manager.check(() -> auth, null).isGranted()).isTrue();
    }
}
