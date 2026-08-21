package io.forgepilot.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    @ConditionalOnProperty(name="forgepilot.auth.oauth-enabled",havingValue="false",matchIfMissing=true)
    SecurityFilterChain localSecurity(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Configuration
    @ConditionalOnProperty(name="forgepilot.auth.oauth-enabled",havingValue="true")
    static class OAuthSecurity {
        @Bean
        SecurityFilterChain oauthSecurityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/index.html", "/assets/**", "/login", "/actuator/health", "/api/auth/providers", "/error").permitAll()
                    .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth.defaultSuccessUrl("/", true))
                .logout(logout -> logout.logoutSuccessUrl("/"));
            return http.build();
        }
    }
}
