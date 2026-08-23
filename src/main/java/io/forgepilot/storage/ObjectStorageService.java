package io.forgepilot.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.platform.PlatformStateStore;
import io.forgepilot.workspace.WorkspaceService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Project-scoped object storage metadata and content for generated apps. */
@Service
public class ObjectStorageService {
    private static final String STATE = "object-storage.json";
    private static final int MAX_OBJECT_BYTES = 5 * 1024 * 1024;
    private final PlatformStateStore stateStore;
    private final WorkspaceService workspaceService;
    private final Map<UUID, LinkedHashMap<String, StoredObject>> objects;

    public ObjectStorageService(PlatformStateStore stateStore, WorkspaceService workspaceService) {
        this.stateStore = stateStore;
        this.workspaceService = workspaceService;
        this.objects = new LinkedHashMap<>(stateStore.read(STATE,
                new TypeReference<Map<UUID, LinkedHashMap<String, StoredObject>>>() {}, LinkedHashMap::new));
    }

    public synchronized List<ObjectSummary> list(UUID projectId) {
        return bucket(projectId).values().stream()
                .map(o -> new ObjectSummary(o.key(), o.contentType(), o.size(), o.updatedAt()))
                .toList();
    }

    public synchronized StoredObject get(UUID projectId, String key) {
        StoredObject value = bucket(projectId).get(key);
        if (value == null) throw new IllegalArgumentException("Object not found: " + key);
        return value;
    }

    public synchronized ObjectSummary put(UUID projectId, String key, String contentType, String base64) {
        validateKey(key);
        byte[] bytes;
        try { bytes = Base64.getDecoder().decode(base64 == null ? "" : base64); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("Invalid base64 payload"); }
        if (bytes.length > MAX_OBJECT_BYTES) throw new IllegalArgumentException("Object exceeds 5 MB limit");
        StoredObject object = new StoredObject(key,
                contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType,
                bytes.length, Base64.getEncoder().encodeToString(bytes), Instant.now());
        bucket(projectId).put(key, object);
        persist();
        workspaceService.putFile(projectId, "storage/manifest.json", manifest(projectId));
        workspaceService.snapshot(projectId, "Storage object: " + key);
        return new ObjectSummary(object.key(), object.contentType(), object.size(), object.updatedAt());
    }

    public synchronized void delete(UUID projectId, String key) {
        if (bucket(projectId).remove(key) == null) throw new IllegalArgumentException("Object not found: " + key);
        persist();
        workspaceService.putFile(projectId, "storage/manifest.json", manifest(projectId));
        workspaceService.snapshot(projectId, "Storage delete: " + key);
    }

    public synchronized String scaffold(UUID projectId) {
        String ts = """
                export async function uploadObject(projectId:string,key:string,file:File){
                  const base64=await new Promise<string>((resolve,reject)=>{const r=new FileReader();r.onload=()=>resolve(String(r.result).split(',')[1]||'');r.onerror=reject;r.readAsDataURL(file)});
                  const response=await fetch(`/api/projects/${projectId}/storage/objects`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({key,contentType:file.type,base64})});
                  if(!response.ok) throw new Error('Upload failed'); return response.json();
                }
                export async function listObjects(projectId:string){const r=await fetch(`/api/projects/${projectId}/storage/objects`);if(!r.ok)throw new Error('List failed');return r.json()}
                """;
        workspaceService.putFile(projectId, "src/lib/storage.ts", ts);
        workspaceService.putFile(projectId, "storage/manifest.json", manifest(projectId));
        workspaceService.snapshot(projectId, "Object storage scaffold");
        return "src/lib/storage.ts";
    }

    private LinkedHashMap<String, StoredObject> bucket(UUID projectId) {
        return objects.computeIfAbsent(projectId, ignored -> new LinkedHashMap<>());
    }
    private void persist() { stateStore.write(STATE, objects); }
    private void validateKey(String key) { if (key == null || key.isBlank() || key.startsWith("/") || key.contains("..")) throw new IllegalArgumentException("Invalid object key"); }
    private String manifest(UUID projectId) {
        List<String> lines = new ArrayList<>();
        lines.add("{\n  \"objects\": [");
        List<ObjectSummary> all = list(projectId);
        for (int i=0;i<all.size();i++) { ObjectSummary o=all.get(i); lines.add("    {\"key\":\""+o.key().replace("\"","\\\"")+"\",\"contentType\":\""+o.contentType()+"\",\"size\":"+o.size()+"}"+(i<all.size()-1?",":"")); }
        lines.add("  ]\n}\n");
        return String.join("\n", lines);
    }

    public record StoredObject(String key,String contentType,long size,String base64,Instant updatedAt) {}
    public record ObjectSummary(String key,String contentType,long size,Instant updatedAt) {}
}
