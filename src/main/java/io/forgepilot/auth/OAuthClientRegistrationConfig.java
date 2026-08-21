package io.forgepilot.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnProperty(name = "forgepilot.auth.oauth-enabled", havingValue = "true")
public class OAuthClientRegistrationConfig {
    @Bean
    ClientRegistrationRepository clientRegistrationRepository(
            @Value("${GOOGLE_CLIENT_ID:}") String googleClientId,
            @Value("${GOOGLE_CLIENT_SECRET:}") String googleClientSecret,
            @Value("${GITHUB_OAUTH_CLIENT_ID:}") String githubClientId,
            @Value("${GITHUB_OAUTH_CLIENT_SECRET:}") String githubClientSecret) {
        List<ClientRegistration> registrations = new ArrayList<>();
        if (present(googleClientId, googleClientSecret)) {
            registrations.add(CommonOAuth2Provider.GOOGLE.getBuilder("google")
                    .clientId(googleClientId)
                    .clientSecret(googleClientSecret)
                    .clientName("Google")
                    .scope("openid", "profile", "email")
                    .build());
        }
        if (present(githubClientId, githubClientSecret)) {
            registrations.add(CommonOAuth2Provider.GITHUB.getBuilder("github")
                    .clientId(githubClientId)
                    .clientSecret(githubClientSecret)
                    .clientName("GitHub")
                    .scope("read:user", "user:email")
                    .build());
        }
        if (registrations.isEmpty()) {
            throw new IllegalStateException("OAuth is enabled but neither Google nor GitHub credentials are fully configured");
        }
        return new InMemoryClientRegistrationRepository(registrations);
    }

    private boolean present(String... values) {
        for (String value : values) if (value == null || value.isBlank()) return false;
        return true;
    }
}
