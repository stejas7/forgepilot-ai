package io.forgepilot.auth;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class AuthStartupValidator implements ApplicationRunner {
    private final AuthPolicyProperties properties;
    private final Environment environment;

    public AuthStartupValidator(AuthPolicyProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean production = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (!production) return;
        if (!properties.isOauthEnabled()) {
            throw new IllegalStateException("Production authentication must be enabled");
        }
        if (properties.getPublicUrl().isBlank() || !properties.getPublicUrl().startsWith("https://")) {
            throw new IllegalStateException("Production authentication requires an HTTPS forgepilot.auth.public-url");
        }
        if (!properties.isSsoRequired()) {
            throw new IllegalStateException("Production workspace must require SSO");
        }
    }
}
