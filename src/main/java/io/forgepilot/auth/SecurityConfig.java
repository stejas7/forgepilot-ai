package io.forgepilot.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
        SecurityFilterChain oauthSecurityFilterChain(HttpSecurity http,
                                                     WorkspaceAccessAuthorizationManager workspaceAccess) throws Exception {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/index.html", "/assets/**", "/login", "/actuator/health", "/api/auth/providers", "/api/auth/readiness", "/error").permitAll()
                    .requestMatchers("/oauth2/authorization/google", "/oauth2/authorization/github", "/login/oauth2/code/**").permitAll()
                    .anyRequest().access(workspaceAccess))
                .oauth2Login(oauth -> oauth
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/?authError=true"))
                .logout(logout -> logout
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
                    .logoutSuccessUrl("/"));
            return http.build();
        }
    }
}
