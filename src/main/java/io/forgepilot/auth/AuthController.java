package io.forgepilot.auth;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
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
    public AuthController(ObjectProvider<ClientRegistrationRepository> registrations){this.registrations=registrations;}

    @GetMapping("/me")
    public Map<String,Object> me(Authentication authentication){
        return Map.of("authenticated", authentication != null && authentication.isAuthenticated(),
                "name", authentication == null ? "" : authentication.getName(),
                "authorities", authentication == null ? List.of() : authentication.getAuthorities().stream().map(Object::toString).toList());
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
