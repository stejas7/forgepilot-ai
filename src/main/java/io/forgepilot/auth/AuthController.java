package io.forgepilot.auth;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final ObjectProvider<ClientRegistrationRepository> registrations;
    private final RoleMappingService roleMappingService;

    public AuthController(ObjectProvider<ClientRegistrationRepository> registrations, RoleMappingService roleMappingService){
        this.registrations=registrations;
        this.roleMappingService=roleMappingService;
    }

    @GetMapping("/me")
    public Map<String,Object> me(Authentication authentication){
        boolean authenticated=authentication != null && authentication.isAuthenticated();
        Object principal=authentication == null ? null : authentication.getPrincipal();
        List<String> roles=principal instanceof OAuth2User oauth2User ? roleMappingService.roles(oauth2User) : List.of();
        return Map.of("authenticated", authenticated,
                "name", displayName(authentication),
                "authorities", authentication == null ? List.of() : authentication.getAuthorities().stream().map(Object::toString).toList(),
                "roles", roles);
    }

    private String displayName(Authentication authentication) {
        if (authentication == null) return "";
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            for (String key : List.of("name", "login", "preferred_username", "email")) {
                Object value = oauth2User.getAttributes().get(key);
                if (value != null) {
                    String candidate = value.toString().trim();
                    if (!candidate.isBlank() && !candidate.matches("\\d+")) return candidate;
                }
            }
        }
        String name = authentication.getName();
        if (name == null || name.isBlank() || name.trim().matches("\\d+")) return "Signed in user";
        return name.trim();
    }

    @GetMapping("/providers")
    public List<Map<String,String>> providers(){
        List<Map<String,String>> result=new ArrayList<>();
        ClientRegistrationRepository repository=registrations.getIfAvailable();
        if(repository instanceof InMemoryClientRegistrationRepository memory){
            for(ClientRegistration registration:memory){
                result.add(Map.of("id",registration.getRegistrationId(),"name",registration.getClientName(),"loginUrl","/oauth2/authorization/"+registration.getRegistrationId()));
            }
        }
        return result;
    }
}
