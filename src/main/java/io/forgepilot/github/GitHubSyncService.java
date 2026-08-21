package io.forgepilot.github;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.forgepilot.platform.PlatformStateStore;
import io.forgepilot.workspace.WorkspaceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GitHubSyncService {
    private static final String STATE="github-connections.json";
    private final PlatformStateStore stateStore;
    private final WorkspaceService workspaceService;
    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final String token;
    private final Map<UUID,Connection> connections;

    public GitHubSyncService(PlatformStateStore stateStore,WorkspaceService workspaceService,RestClient.Builder builder,ObjectMapper objectMapper,
                             @Value("${forgepilot.github.token:${GITHUB_TOKEN:}}") String token){
        this.stateStore=stateStore;this.workspaceService=workspaceService;this.client=builder.baseUrl("https://api.github.com").build();this.objectMapper=objectMapper;this.token=token;
        this.connections=new LinkedHashMap<>(stateStore.read(STATE,new TypeReference<Map<UUID,Connection>>() {},LinkedHashMap::new));
    }

    public boolean configured(){return StringUtils.hasText(token);}
    public synchronized Connection get(UUID projectId){return connections.get(projectId);}

    public synchronized Connection createRepository(UUID projectId,String name,boolean privateRepo){
        requireConfigured();
        JsonNode created=requestPost("/user/repos",Map.of("name",name,"private",privateRepo,"auto_init",true));
        String owner=created.path("owner").path("login").asText();
        String branch=created.path("default_branch").asText("main");
        return connect(projectId,owner,name,branch);
    }

    public synchronized Connection connect(UUID projectId,String owner,String repo,String branch){
        String resolved=branch==null||branch.isBlank()?defaultBranch(owner.trim(),repo.trim()):branch.trim();
        String head=remoteHead(owner.trim(),repo.trim(),resolved);
        Connection connection=new Connection(projectId,owner.trim(),repo.trim(),resolved,"CONNECTED",null,null,Instant.now(),head,fingerprint(projectId));
        connections.put(projectId,connection);persist();return connection;
    }
    public synchronized void disconnect(UUID projectId){connections.remove(projectId);persist();}

    public synchronized SyncStatus inspect(UUID projectId){
        Connection c=requireConnection(projectId);String remote=remoteHead(c.owner(),c.repo(),c.branch());String local=fingerprint(projectId);
        boolean remoteChanged=c.remoteHeadSha()!=null&&!c.remoteHeadSha().equals(remote);
        boolean localChanged=c.localFingerprint()!=null&&!c.localFingerprint().equals(local);
        String state=remoteChanged&&localChanged?"CONFLICT":remoteChanged?"REMOTE_AHEAD":localChanged?"LOCAL_AHEAD":"SYNCED";
        return new SyncStatus(state,remoteChanged,localChanged,remote,local);
    }

    public synchronized SyncResult push(UUID projectId){return push(projectId,false);}
    public synchronized SyncResult push(UUID projectId,boolean force){
        requireConfigured();Connection c=requireConnection(projectId);SyncStatus status=inspect(projectId);
        if("CONFLICT".equals(status.status())&&!force)throw new IllegalStateException("GitHub conflict detected. Pull/resolve or force push explicitly.");
        List<String> changed=new ArrayList<>();
        for(WorkspaceService.WorkspaceFile file:workspaceService.listFiles(projectId)){
            String sha=remoteSha(c,file.path());
            Map<String,Object> payload=new LinkedHashMap<>();payload.put("message","ForgePilot sync: "+file.path());payload.put("content",Base64.getEncoder().encodeToString(file.content().getBytes(StandardCharsets.UTF_8)));payload.put("branch",c.branch());if(sha!=null)payload.put("sha",sha);
            requestPut("/repos/{owner}/{repo}/contents/{path}",payload,c.owner(),c.repo(),file.path());changed.add(file.path());
        }
        String head=remoteHead(c.owner(),c.repo(),c.branch());String local=fingerprint(projectId);
        Connection updated=new Connection(c.projectId(),c.owner(),c.repo(),c.branch(),"SYNCED",Instant.now(),c.lastPulledAt(),Instant.now(),head,local);connections.put(projectId,updated);persist();
        return new SyncResult("PUSH",changed,updated);
    }

    public synchronized SyncResult pull(UUID projectId){return pull(projectId,false);}
    public synchronized SyncResult pull(UUID projectId,boolean force){
        requireConfigured();Connection c=requireConnection(projectId);SyncStatus status=inspect(projectId);
        if("CONFLICT".equals(status.status())&&!force)throw new IllegalStateException("GitHub conflict detected. Resolve or force pull explicitly.");
        JsonNode tree=requestGet("/repos/{owner}/{repo}/git/trees/{branch}?recursive=1",c.owner(),c.repo(),c.branch());List<String> changed=new ArrayList<>();
        for(JsonNode node:tree.path("tree")){
            if(!"blob".equals(node.path("type").asText()))continue;String path=node.path("path").asText();if(path.startsWith(".git"))continue;
            JsonNode blob=requestGet("/repos/{owner}/{repo}/git/blobs/{sha}",c.owner(),c.repo(),node.path("sha").asText());if(!"base64".equals(blob.path("encoding").asText()))continue;
            String content=new String(Base64.getMimeDecoder().decode(blob.path("content").asText()),StandardCharsets.UTF_8);workspaceService.putFile(projectId,path,content);changed.add(path);
        }
        workspaceService.snapshot(projectId,"GitHub pull: "+c.owner()+"/"+c.repo());
        String head=remoteHead(c.owner(),c.repo(),c.branch());String local=fingerprint(projectId);
        Connection updated=new Connection(c.projectId(),c.owner(),c.repo(),c.branch(),"SYNCED",c.lastPushedAt(),Instant.now(),Instant.now(),head,local);connections.put(projectId,updated);persist();return new SyncResult("PULL",changed,updated);
    }

    private String defaultBranch(String owner,String repo){return requestGet("/repos/{owner}/{repo}",owner,repo).path("default_branch").asText("main");}
    private String remoteHead(String owner,String repo,String branch){return requestGet("/repos/{owner}/{repo}/git/ref/heads/{branch}",owner,repo,branch).path("object").path("sha").asText(null);}
    private String remoteSha(Connection c,String path){try{return requestGet("/repos/{owner}/{repo}/contents/{path}?ref={branch}",c.owner(),c.repo(),path,c.branch()).path("sha").asText(null);}catch(HttpClientErrorException.NotFound ignored){return null;}}
    private String fingerprint(UUID projectId){
        try{MessageDigest digest=MessageDigest.getInstance("SHA-256");workspaceService.listFiles(projectId).stream().sorted(Comparator.comparing(WorkspaceService.WorkspaceFile::path)).forEach(file->{digest.update(file.path().getBytes(StandardCharsets.UTF_8));digest.update((byte)0);digest.update(file.content().getBytes(StandardCharsets.UTF_8));});return HexFormat.of().formatHex(digest.digest());}catch(Exception e){throw new IllegalStateException("Unable to fingerprint workspace",e);}
    }
    private JsonNode requestGet(String uri,Object...vars){String raw=client.get().uri(uri,vars).header("Authorization","Bearer "+token).header("Accept","application/vnd.github+json").retrieve().body(String.class);try{return objectMapper.readTree(raw);}catch(Exception e){throw new IllegalStateException("Unable to parse GitHub response",e);}}
    private JsonNode requestPost(String uri,Object body){String raw=client.post().uri(uri).contentType(MediaType.APPLICATION_JSON).header("Authorization","Bearer "+token).header("Accept","application/vnd.github+json").body(body).retrieve().body(String.class);try{return objectMapper.readTree(raw);}catch(Exception e){throw new IllegalStateException("Unable to parse GitHub response",e);}}
    private void requestPut(String uri,Object body,Object...vars){client.put().uri(uri,vars).contentType(MediaType.APPLICATION_JSON).header("Authorization","Bearer "+token).header("Accept","application/vnd.github+json").body(body).retrieve().toBodilessEntity();}
    private Connection requireConnection(UUID projectId){Connection c=connections.get(projectId);if(c==null)throw new IllegalStateException("Project is not connected to GitHub");return c;}
    private void requireConfigured(){if(!configured())throw new IllegalStateException("GITHUB_TOKEN is not configured");}
    private void persist(){stateStore.write(STATE,connections);}

    public record Connection(UUID projectId,String owner,String repo,String branch,String status,Instant lastPushedAt,Instant lastPulledAt,Instant updatedAt,String remoteHeadSha,String localFingerprint){}
    public record SyncStatus(String status,boolean remoteChanged,boolean localChanged,String remoteHeadSha,String localFingerprint){}
    public record SyncResult(String direction,List<String> files,Connection connection){}
}
