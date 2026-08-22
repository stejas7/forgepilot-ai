package io.forgepilot.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OAuthClientRegistrationConfigTest {
    private final OAuthClientRegistrationConfig config = new OAuthClientRegistrationConfig();

    @Test
    void buildsGoogleAndGithubProvidersFromCompleteCredentials() {
        var repository = config.clientRegistrationRepository(
                "google-id", "google-secret",
                "github-id", "github-secret");
        assertThat(repository).isInstanceOf(InMemoryClientRegistrationRepository.class);
        var memory = (InMemoryClientRegistrationRepository) repository;
        assertThat(memory.findByRegistrationId("google")).isNotNull();
        assertThat(memory.findByRegistrationId("github")).isNotNull();
    }

    @Test
    void failsClosedWhenOauthEnabledWithoutAnyCompleteProvider() {
        assertThatThrownBy(() -> config.clientRegistrationRepository("", "", "", ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no complete provider credentials");
    }
}
