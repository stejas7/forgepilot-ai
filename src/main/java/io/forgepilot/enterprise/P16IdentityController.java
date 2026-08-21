package io.forgepilot.enterprise;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.platform.PlatformStateStore;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/enterprise/identity")
public class P16IdentityController {
    private final P16IdentityService service;
    public P16IdentityController(P16IdentityService service){this.service=service;}

    @GetMapping public P16IdentityService.State state(){return service.state();}
    @PostMapping("/profiles") public P16IdentityService.IdentityProfile profile(@RequestBody ProfileRequest r){return service.upsertProfile(r.name(),r.protocol(),r.issuer(),r.clientId(),r.authorizationEndpoint(),r.enforced());}
    @PostMapping("/domains/{domain}") public void domain(@PathVariable String domain,@RequestBody DomainRequest r){service.mapDomain(domain,r.profileId(),r.enforced());}
    @GetMapping("/discover") public P16IdentityService.Discovery discover(@RequestParam String email){return service.discover(email);}
    @GetMapping("/authorize/{profileId}") public P16IdentityService.Authorization authorization(@PathVariable UUID profileId,@RequestParam String redirectUri,@RequestParam(defaultValue="openid profile email") String scope){return service.authorization(profileId,redirectUri,scope);}
    @PostMapping("/scim/users") public P16IdentityService.ScimUser user(@RequestBody ScimUserRequest r){return service.upsertUser(r.externalId(),r.email(),r.active(),r.groups());}
    @PostMapping("/scim/groups") public P16IdentityService.ScimGroup group(@RequestBody ScimGroupRequest r){return service.upsertGroup(r.externalId(),r.name(),r.members());}
    @PostMapping("/role-mappings") public void role(@RequestBody RoleMappingRequest r){service.roleMapping(r.group(),r.role());}
    @PostMapping("/sessions") public P16IdentityService.Session session(@RequestBody SessionRequest r){return service.createSession(r.subject(),r.profileId(),r.ttlSeconds());}
    @DeleteMapping("/sessions/{id}") public void revoke(@PathVariable UUID id){service.revoke(id);}
    @PostMapping("/emergency-access") public P16IdentityService.EmergencyAccess emergency(@RequestBody EmergencyRequest r){return service.emergency(r.actor(),r.reason(),r.minutes());}
    @GetMapping("/diagnostics") public List<P16IdentityService.IdentityEvent> diagnostics(){return service.state().events();}

    public record ProfileRequest(String name,String protocol,String issuer,String clientId,String authorizationEndpoint,boolean enforced){}
    public record DomainRequest(UUID profileId,boolean enforced){}
    public record ScimUserRequest(String externalId,String email,boolean active,List<String> groups){}
    public record ScimGroupRequest(String externalId,String name,List<String> members){}
    public record RoleMappingRequest(String group,String role){}
    public record SessionRequest(String subject,UUID profileId,long ttlSeconds){}
    public record EmergencyRequest(String actor,String reason,int minutes){}
}

@Service
class P16IdentityService {
    private static final String FILE="p16-identity.json";
    private final PlatformStateStore store;
    private State state;
    P16IdentityService(PlatformStateStore store){this.store=store;this.state=store.read(FILE,new TypeReference<State>(){},State::empty);}
    synchronized State state(){return state;}
    synchronized IdentityProfile upsertProfile(String name,String protocol,String issuer,String clientId,String authorizationEndpoint,boolean enforced){
        String p=protocol==null?"OIDC":protocol.toUpperCase(Locale.ROOT);if(!List.of("OIDC","SAML").contains(p))throw new IllegalArgumentException("protocol must be OIDC or SAML");
        IdentityProfile x=new IdentityProfile(UUID.randomUUID(),name,p,issuer,clientId,authorizationEndpoint,enforced,Instant.now());state.profiles().add(x);event("PROFILE_UPSERT",name);persist();return x;
    }
    synchronized void mapDomain(String domain,UUID profileId,boolean enforced){profile(profileId);state.domains().put(domain.toLowerCase(Locale.ROOT),new DomainBinding(profileId,enforced));event("DOMAIN_MAPPING",domain);persist();}
    synchronized Discovery discover(String email){String domain=email!=null&&email.contains("@")?email.substring(email.lastIndexOf('@')+1).toLowerCase(Locale.ROOT):"";DomainBinding b=state.domains().get(domain);return new Discovery(domain,b==null?null:b.profileId(),b!=null&&b.enforced());}
    synchronized Authorization authorization(UUID profileId,String redirectUri,String scope){IdentityProfile p=profile(profileId);if(!"OIDC".equals(p.protocol()))return new Authorization(p.id(),p.protocol(),null,"SAML profile configured; assertion processing is provider-driven");String endpoint=p.authorizationEndpoint();if(endpoint==null||endpoint.isBlank())throw new IllegalStateException("authorizationEndpoint is required");String q="client_id="+enc(p.clientId())+"&redirect_uri="+enc(redirectUri)+"&response_type=code&scope="+enc(scope)+"&state="+UUID.randomUUID();return new Authorization(p.id(),p.protocol(),endpoint+(endpoint.contains("?")?"&":"?")+q,"OIDC authorization URL generated");}
    synchronized ScimUser upsertUser(String externalId,String email,boolean active,List<String> groups){ScimUser u=new ScimUser(externalId,email,active,groups==null?List.of():List.copyOf(groups),roleFor(groups),Instant.now());state.users().put(externalId,u);event(active?"SCIM_USER_UPSERT":"SCIM_USER_DEPROVISION",email);persist();return u;}
    synchronized ScimGroup upsertGroup(String externalId,String name,List<String> members){ScimGroup g=new ScimGroup(externalId,name,members==null?List.of():List.copyOf(members),Instant.now());state.groups().put(externalId,g);event("SCIM_GROUP_UPSERT",name);persist();return g;}
    synchronized void roleMapping(String group,String role){state.roleMappings().put(group,role);event("ROLE_MAPPING",group+"->"+role);persist();}
    synchronized Session createSession(String subject,UUID profileId,long ttlSeconds){profile(profileId);long ttl=Math.max(60,Math.min(ttlSeconds<=0?3600:ttlSeconds,86400));Session s=new Session(UUID.randomUUID(),subject,profileId,Instant.now(),Instant.now().plusSeconds(ttl),"ACTIVE");state.sessions().put(s.id(),s);event("SESSION_CREATE",subject);persist();return s;}
    synchronized void revoke(UUID id){Session s=state.sessions().get(id);if(s!=null){state.sessions().put(id,new Session(s.id(),s.subject(),s.profileId(),s.createdAt(),s.expiresAt(),"REVOKED"));event("SESSION_REVOKE",s.subject());persist();}}
    synchronized EmergencyAccess emergency(String actor,String reason,int minutes){int ttl=Math.max(5,Math.min(minutes<=0?30:minutes,60));EmergencyAccess e=new EmergencyAccess(UUID.randomUUID(),actor,reason,Instant.now(),Instant.now().plusSeconds(ttl*60L));state.emergencyAccess().add(e);event("EMERGENCY_ACCESS",actor+": "+reason);persist();return e;}
    private IdentityProfile profile(UUID id){return state.profiles().stream().filter(x->x.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Identity profile not found"));}
    private String roleFor(List<String> groups){if(groups!=null)for(String g:groups)if(state.roleMappings().containsKey(g))return state.roleMappings().get(g);return "VIEWER";}
    private void event(String action,String detail){state.events().add(0,new IdentityEvent(Instant.now(),action,detail));if(state.events().size()>500)state.events().remove(state.events().size()-1);}
    private String enc(String v){return URLEncoder.encode(v==null?"":v, StandardCharsets.UTF_8);}
    private void persist(){store.write(FILE,state);}
    record IdentityProfile(UUID id,String name,String protocol,String issuer,String clientId,String authorizationEndpoint,boolean enforced,Instant updatedAt){}
    record DomainBinding(UUID profileId,boolean enforced){}
    record Discovery(String domain,UUID profileId,boolean enforced){}
    record Authorization(UUID profileId,String protocol,String url,String message){}
    record ScimUser(String externalId,String email,boolean active,List<String> groups,String role,Instant updatedAt){}
    record ScimGroup(String externalId,String name,List<String> members,Instant updatedAt){}
    record Session(UUID id,String subject,UUID profileId,Instant createdAt,Instant expiresAt,String status){}
    record EmergencyAccess(UUID id,String actor,String reason,Instant createdAt,Instant expiresAt){}
    record IdentityEvent(Instant at,String action,String detail){}
    record State(List<IdentityProfile> profiles,Map<String,DomainBinding> domains,Map<String,ScimUser> users,Map<String,ScimGroup> groups,Map<String,String> roleMappings,Map<UUID,Session> sessions,List<EmergencyAccess> emergencyAccess,List<IdentityEvent> events){static State empty(){return new State(new ArrayList<>(),new LinkedHashMap<>(),new LinkedHashMap<>(),new LinkedHashMap<>(),new LinkedHashMap<>(),new LinkedHashMap<>(),new ArrayList<>(),new ArrayList<>());}}
}