package io.forgepilot.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RoleMappingServiceTest {
    private final RoleMappingService service = new RoleMappingService("forgepilot-admins,platform-owners", "forgepilot-editors");

    @Test
    void defaultsUnknownUsersToViewer() {
        assertThat(service.roles(user(Map.of("sub", "u1", "groups", List.of("employees")))))
                .containsExactly("VIEWER");
    }

    @Test
    void mapsEditorGroup() {
        assertThat(service.roles(user(Map.of("sub", "u2", "groups", List.of("FORGEPILOT-EDITORS")))))
                .containsExactly("EDITOR", "VIEWER");
    }

    @Test
    void mapsAdminGroupToOwnerAndAdminRoles() {
        assertThat(service.roles(user(Map.of("sub", "u3", "groups", List.of("platform-owners")))))
                .containsExactly("OWNER", "ADMIN", "EDITOR", "VIEWER");
    }

    private OAuth2User user(Map<String,Object> attributes) {
        return new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("OAUTH2_USER")), attributes, "sub");
    }
}
