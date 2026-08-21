package io.forgepilot.auth;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthReadinessController {
    private final AuthPolicyProperties properties;
    private final ObjectProvider<ClientRegistrationRepository> registrations;

    public AuthReadinessController(AuthPolicyProperties properties, ObjectProvider<ClientRegistrationRepository> registrations) {
        this.properties = properties;
        this.registrations = registrations;
    }

    @GetMapping("/readiness")
    public Map<String, Object> readiness() {
        List<String> providers = new ArrayList<>();
        ClientRegistrationRepository repository = registrations.getIfAvailable();
        if (repository instanceof InMemoryClientRegistrationRepository memory) {
            for (ClientRegistration registration : memory) providers.add(registration.getRegistrationId());
        }
        boolean publicUrlReady = !properties.isOauthEnabled() || properties.getPublicUrl().startsWith("https://");
        boolean providerReady = !properties.isOauthEnabled() || !providers.isEmpty();
        boolean ready = publicUrlReady && providerReady;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ready", ready);
        result.put("oauthEnabled", properties.isOauthEnabled());
        result.put("ssoRequired", properties.isSsoRequired());
        result.put("configuredProviders", providers);
        result.put("approvedDomainCount", properties.getApprovedDomains().size());
        result.put("publicHttpsConfigured", publicUrlReady);
        result.put("secretValuesExposed", false);
        return result;
    }
}
