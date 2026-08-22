package io.forgepilot.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthStartupValidatorTest {
    @Test
    void productionRequiresOauth() {
        AuthPolicyProperties p = new AuthPolicyProperties();
        p.setOauthEnabled(false);
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        assertThrows(IllegalStateException.class, () -> new AuthStartupValidator(p, env).run(new DefaultApplicationArguments()));
    }

    @Test
    void productionRequiresHttpsAndSso() {
        AuthPolicyProperties p = new AuthPolicyProperties();
        p.setOauthEnabled(true);
        p.setSsoRequired(true);
        p.setPublicUrl("https://forgepilot-ai.duckdns.org");
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        assertDoesNotThrow(() -> new AuthStartupValidator(p, env).run(new DefaultApplicationArguments()));
    }

    @Test
    void nonProductionRemainsDeveloperFriendly() {
        AuthPolicyProperties p = new AuthPolicyProperties();
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("test");
        assertDoesNotThrow(() -> new AuthStartupValidator(p, env).run(new DefaultApplicationArguments()));
    }
}
