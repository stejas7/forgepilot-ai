package io.forgepilot.connectors;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.platform.PlatformStateStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Durable connector catalog and project-scoped connection metadata. */
@Service
public class ConnectorService {
    private static final String STATE="connectors.json";
    private final PlatformStateStore stateStore;
    private final Map<UUID,Map<String,Connection>> connections;

    public ConnectorService(PlatformStateStore stateStore){
        this.stateStore=stateStore;
        this.connections=new LinkedHashMap<>(stateStore.read(STATE,new TypeReference<Map<UUID,Map<String,Connection>>>(){},LinkedHashMap::new));
    }

    public List<CatalogItem> catalog(){return List.of(
            new CatalogItem("stripe","Stripe","payments","API_KEY",List.of("secretKey","webhookSecret")),
            new CatalogItem("resend","Resend","email","API_KEY",List.of("apiKey","fromEmail")),
            new CatalogItem("rest","REST API","api","API_KEY",List.of("baseUrl","apiKey")),
            new CatalogItem("webhook","Webhook","automation","URL",List.of("url")),
            new CatalogItem("supabase","Supabase","database","API_KEY",List.of("url","anonKey")),
            new CatalogItem("slack","Slack","collaboration","OAUTH",List.of("workspace"))
    );}

    public synchronized List<Connection> list(UUID projectId){return List.copyOf(connections.computeIfAbsent(projectId,ignored->new LinkedHashMap<>()).values());}

    public synchronized Connection connect(UUID projectId,String connectorId,Map<String,String> config){
        CatalogItem item=catalog().stream().filter(c->c.id().equals(connectorId)).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown connector: "+connectorId));
        Map<String,String> safe=new LinkedHashMap<>();
        if(config!=null)config.forEach((key,value)->safe.put(key,isSecret(key)?mask(value):value));
        Connection connection=new Connection(connectorId,item.name(),"CONNECTED",Map.copyOf(safe),Instant.now(),Instant.now());
        connections.computeIfAbsent(projectId,ignored->new LinkedHashMap<>()).put(connectorId,connection);persist();return connection;
    }

    public synchronized Connection health(UUID projectId,String connectorId){
        Connection current=require(projectId,connectorId);
        String status=current.config().isEmpty()?"NEEDS_CONFIGURATION":"HEALTHY";
        Connection updated=new Connection(current.connectorId(),current.name(),status,current.config(),current.connectedAt(),Instant.now());
        connections.get(projectId).put(connectorId,updated);persist();return updated;
    }

    public synchronized void disconnect(UUID projectId,String connectorId){Map<String,Connection> project=connections.get(projectId);if(project!=null){project.remove(connectorId);persist();}}

    private Connection require(UUID projectId,String connectorId){Connection c=connections.computeIfAbsent(projectId,ignored->new LinkedHashMap<>()).get(connectorId);if(c==null)throw new IllegalStateException("Connector is not connected");return c;}
    private boolean isSecret(String key){String lower=key.toLowerCase();return lower.contains("key")||lower.contains("secret")||lower.contains("token")||lower.contains("password");}
    private String mask(String value){if(value==null||value.isBlank())return"";return"••••"+value.substring(Math.max(0,value.length()-Math.min(4,value.length())));}
    private void persist(){stateStore.write(STATE,connections);}

    public record CatalogItem(String id,String name,String category,String authType,List<String> fields){}
    public record Connection(String connectorId,String name,String status,Map<String,String> config,Instant connectedAt,Instant checkedAt){}
}
