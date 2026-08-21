package io.forgepilot.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthAuditListener {
    private static final Logger log = LoggerFactory.getLogger(AuthAuditListener.class);

    @EventListener
    public void success(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        log.info("auth_event type=LOGIN_SUCCESS principal={} authorities={}", safe(authentication.getName()), authentication.getAuthorities().size());
    }

    @EventListener
    public void failure(AbstractAuthenticationFailureEvent event) {
        Authentication authentication = event.getAuthentication();
        log.warn("auth_event type=LOGIN_FAILURE principal={} reason={}", safe(authentication == null ? null : authentication.getName()), event.getException().getClass().getSimpleName());
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) return "anonymous";
        int at = value.indexOf('@');
        return at > 1 ? value.substring(0, 1) + "***" + value.substring(at) : "masked";
    }
}
