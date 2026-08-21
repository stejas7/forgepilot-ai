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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Project repository ownership and two-way synchronization adapter. */
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
    public synchronized Connection connect(UUID projectId,String owner,String repo,String branch){
        Connection connection=new Connection(projectId,owner.trim(),repo.trim(),branch==null||branch.isBlank()?"main":branch.trim(),"CONNECTED",null,null,Instant.now());
        connections.put(projectId,connection);persist();return connection;
    }
    public synchronized void disconnect(UUID projectId){connections.remove(projectId);persist();}

    public synchronized SyncResult push(UUID projectId){
        requireConfigured();Connection c=requireConnection(projectId);List<String> changed=new ArrayList<>();
        for(WorkspaceService.WorkspaceFile file:workspaceService.listFiles(projectId)){
            String sha=remoteSha(c,file.path());
            Map<String,Object> payload=new LinkedHashMap<>();payload.put("message","ForgePilot sync: "+file.path());payload.put("content",Base64.getEncoder().encodeToString(file.content().getBytes(StandardCharsets.UTF_8)));payload.put("branch",c.branch());if(sha!=null)payload.put("sha",sha);
            requestPut("/repos/{owner}/{repo}/contents/{path}",payload,c.owner(),c.repo(),file.path());changed.add(file.path());
        }
        Connection updated=new Connection(c.projectId(),c.owner(),c.repo(),c.branch(),"SYNCED",Instant.now(),c.lastPulledAt(),Instant.now());connections.put(projectId,updated);persist();
        return new SyncResult("PUSH",changed,updated);
    }

    public synchronized SyncResult pull(UUID projectId){
        requireConfigured();Connection c=requireConnection(projectId);JsonNode tree=requestGet("/repos/{owner}/{repo}/git/trees/{branch}?recursive=1",c.owner(),c.repo(),c.branch());List<String> changed=new ArrayList<>();
        for(JsonNode node:tree.path("tree")){
            if(!"blob".equals(node.path("type").asText()))continue;String path=node.path("path").asText();if(path.startsWith(".git"))continue;
            JsonNode blob=requestGet("/repos/{owner}/{repo}/git/blobs/{sha}",c.owner(),c.repo(),node.path("sha").asText());String encoding=blob.path("encoding").asText();if(!"base64".equals(encoding))continue;
            String content=new String(Base64.getMimeDecoder().decode(blob.path("content").asText()),StandardCharsets.UTF_8);workspaceService.putFile(projectId,path,content);changed.add(path);
        }
        workspaceService.snapshot(projectId,"GitHub pull: "+c.owner()+"/"+c.repo());
        Connection updated=new Connection(c.projectId(),c.owner(),c.repo(),c.branch(),"SYNCED",c.lastPushedAt(),Instant.now(),Instant.now());connections.put(projectId,updated);persist();return new SyncResult("PULL",changed,updated);
    }

    private String remoteSha(Connection c,String path){try{return requestGet("/repos/{owner}/{repo}/contents/{path}?ref={branch}",c.owner(),c.repo(),path,c.branch()).path("sha").asText(null);}catch(HttpClientErrorException.NotFound ignored){return null;}}
    private JsonNode requestGet(String uri,Object...vars){String raw=client.get().uri(uri,vars).header("Authorization","Bearer "+token).header("Accept","application/vnd.github+json").retrieve().body(String.class);try{return objectMapper.readTree(raw);}catch(Exception e){throw new IllegalStateException("Unable to parse GitHub response",e);}}
    private void requestPut(String uri,Object body,Object...vars){client.put().uri(uri,vars).contentType(MediaType.APPLICATION_JSON).header("Authorization","Bearer "+token).header("Accept","application/vnd.github+json").body(body).retrieve().toBodilessEntity();}
    private Connection requireConnection(UUID projectId){Connection c=connections.get(projectId);if(c==null)throw new IllegalStateException("Project is not connected to GitHub");return c;}
    private void requireConfigured(){if(!configured())throw new IllegalStateException("GITHUB_TOKEN is not configured");}
    private void persist(){stateStore.write(STATE,connections);}

    public record Connection(UUID projectId,String owner,String repo,String branch,String status,Instant lastPushedAt,Instant lastPulledAt,Instant updatedAt){}
    public record SyncResult(String direction,List<String> files,Connection connection){}
}
