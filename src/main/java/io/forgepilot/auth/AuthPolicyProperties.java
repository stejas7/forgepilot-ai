package io.forgepilot.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "forgepilot.auth")
public class AuthPolicyProperties {
    private boolean oauthEnabled;
    private boolean ssoRequired;
    private String publicUrl = "";
    private List<String> approvedDomains = new ArrayList<>();
    private List<String> adminEmails = new ArrayList<>();

    public boolean isOauthEnabled() { return oauthEnabled; }
    public void setOauthEnabled(boolean oauthEnabled) { this.oauthEnabled = oauthEnabled; }
    public boolean isSsoRequired() { return ssoRequired; }
    public void setSsoRequired(boolean ssoRequired) { this.ssoRequired = ssoRequired; }
    public String getPublicUrl() { return publicUrl; }
    public void setPublicUrl(String publicUrl) { this.publicUrl = publicUrl == null ? "" : publicUrl.trim(); }
    public List<String> getApprovedDomains() { return approvedDomains; }
    public void setApprovedDomains(List<String> approvedDomains) { this.approvedDomains = approvedDomains == null ? new ArrayList<>() : approvedDomains; }
    public List<String> getAdminEmails() { return adminEmails; }
    public void setAdminEmails(List<String> adminEmails) { this.adminEmails = adminEmails == null ? new ArrayList<>() : adminEmails; }
}
