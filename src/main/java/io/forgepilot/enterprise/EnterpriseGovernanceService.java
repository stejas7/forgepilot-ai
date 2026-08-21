package io.forgepilot.enterprise;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.platform.PlatformStateStore;
import io.forgepilot.project.Project;
import io.forgepilot.project.ProjectService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Durable enterprise governance core for workspace identity, policy, audit and insights.
 * This implements product controls only; it does not assert external certifications.
 */
@Service
public class EnterpriseGovernanceService {
    private static final String SETTINGS_STATE="enterprise-settings.json";
    private static final String AUDIT_STATE="enterprise-audit.json";

    private final PlatformStateStore stateStore;
    private final ProjectService projectService;
    private EnterpriseSettings settings;
    private final List<AuditEvent> audit;

    public EnterpriseGovernanceService(PlatformStateStore stateStore, ProjectService projectService){
        this.stateStore=stateStore;this.projectService=projectService;
        this.settings=stateStore.read(SETTINGS_STATE,new TypeReference<EnterpriseSettings>(){},EnterpriseGovernanceService::defaults);
        this.audit=new ArrayList<>(stateStore.read(AUDIT_STATE,new TypeReference<List<AuditEvent>>() {},ArrayList::new));
    }

    public synchronized EnterpriseSettings settings(){return settings;}

    public synchronized EnterpriseSettings updateIdentity(IdentityConfig identity){
        settings=new EnterpriseSettings(identity,settings.scim(),settings.policy(),settings.brand(),settings.support(),Instant.now());
        append("IDENTITY_POLICY_UPDATED","workspace",Map.of("ssoEnabled",identity.ssoEnabled(),"protocol",identity.protocol()));persistSettings();return settings;
    }

    public synchronized EnterpriseSettings updateScim(ScimConfig scim){
        settings=new EnterpriseSettings(settings.identity(),scim,settings.policy(),settings.brand(),settings.support(),Instant.now());
        append("SCIM_POLICY_UPDATED","workspace",Map.of("enabled",scim.enabled()));persistSettings();return settings;
    }

    public synchronized EnterpriseSettings updatePolicy(GovernancePolicy policy){
        settings=new EnterpriseSettings(settings.identity(),settings.scim(),policy,settings.brand(),settings.support(),Instant.now());
        append("GOVERNANCE_POLICY_UPDATED","workspace",Map.of("privateByDefault",policy.privateByDefault(),"publishApprovalRequired",policy.publishApprovalRequired()));persistSettings();return settings;
    }

    public synchronized EnterpriseSettings updateBrand(BrandPolicy brand){
        settings=new EnterpriseSettings(settings.identity(),settings.scim(),settings.policy(),brand,settings.support(),Instant.now());
        append("BRAND_POLICY_UPDATED","workspace",Map.of("name",brand.workspaceName()));persistSettings();return settings;
    }

    public synchronized EnterpriseSettings updateSupport(SupportProfile support){
        settings=new EnterpriseSettings(settings.identity(),settings.scim(),settings.policy(),settings.brand(),support,Instant.now());
        append("SUPPORT_PROFILE_UPDATED","workspace",Map.of("tier",support.tier()));persistSettings();return settings;
    }

    public synchronized List<AuditEvent> audit(){return List.copyOf(audit);}

    public synchronized AuditEvent append(String action,String target,Map<String,Object> details){
        AuditEvent event=new AuditEvent(UUID.randomUUID(),Instant.now(),"system",action,target,details==null?Map.of():Map.copyOf(details));
        audit.add(0,event);stateStore.write(AUDIT_STATE,audit);return event;
    }

    public WorkspaceInsights insights(){
        List<Project> projects=projectService.findAll();
        long total=projects.size();
        long drafts=projects.stream().filter(p->p.status()== Project.ProjectStatus.DRAFT).count();
        long active=total-drafts;
        return new WorkspaceInsights(total,active,drafts,0,0,0,0,settings.policy().privateByDefault(),settings.policy().publishApprovalRequired());
    }

    private synchronized void persistSettings(){stateStore.write(SETTINGS_STATE,settings);}

    private static EnterpriseSettings defaults(){
        return new EnterpriseSettings(
                new IdentityConfig(false,"SAML","","",""),
                new ScimConfig(false,"",""),
                new GovernancePolicy(true,true,List.of("OWNER","ADMIN","EDITOR","VIEWER","APPROVER","PUBLISHER"),List.of(),List.of(),true),
                new BrandPolicy("ForgePilot Workspace","","",""),
                new SupportProfile("STANDARD","",""),
                Instant.now());
    }

    public record IdentityConfig(boolean ssoEnabled,String protocol,String provider,String issuer,String metadataUrl){}
    public record ScimConfig(boolean enabled,String baseUrl,String bearerTokenMasked){}
    public record GovernancePolicy(boolean privateByDefault,boolean publishApprovalRequired,List<String> roles,List<String> connectorAllowlist,List<String> modelAllowlist,boolean auditEnabled){}
    public record BrandPolicy(String workspaceName,String logoUrl,String primaryToken,String designSystemRef){}
    public record SupportProfile(String tier,String ownerEmail,String escalationChannel){}
    public record EnterpriseSettings(IdentityConfig identity,ScimConfig scim,GovernancePolicy policy,BrandPolicy brand,SupportProfile support,Instant updatedAt){}
    public record AuditEvent(UUID id,Instant at,String actor,String action,String target,Map<String,Object> details){}
    public record WorkspaceInsights(long totalProjects,long activeProjects,long draftProjects,long externallyPublished,long highReviewPriority,long piiProjects,long abandonedProjects,boolean privateByDefault,boolean publishApprovalRequired){}
}
