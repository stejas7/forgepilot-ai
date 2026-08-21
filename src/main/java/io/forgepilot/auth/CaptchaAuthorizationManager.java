package io.forgepilot.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;
import org.springframework.security.core.Authentication;

@Component
public class CaptchaAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();
        Object verified = request.getSession(false) == null ? null : request.getSession(false).getAttribute(CaptchaController.VERIFIED);
        return new AuthorizationDecision(Boolean.TRUE.equals(verified));
    }
}
