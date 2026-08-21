package io.forgepilot.security;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.enterprise.EnterpriseGovernanceService;
import io.forgepilot.platform.PlatformStateStore;
import io.forgepilot.workspace.WorkspaceService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Lightweight project security scanner for generated applications.
 * It performs deterministic checks over workspace files and stores scan history.
 */
@Service
public class SecurityScanService {
    private static final String STATE="security-scans.json";
    private final PlatformStateStore store;
    private final WorkspaceService workspace;
    private final EnterpriseGovernanceService governance;
    private final Map<UUID,List<ScanReport>> history;

    private static final Pattern SECRET=Pattern.compile("(?i)(api[_-]?key|secret|token|password)\\s*[:=]\\s*[\\\"']?[A-Za-z0-9_\\-]{12,}");
    private static final Pattern DANGEROUS_HTML=Pattern.compile("(?i)(innerHTML\\s*=|document\\.write\\s*\\(|eval\\s*\\()");
    private static final Pattern HTTP_URL=Pattern.compile("http://(?!127\\.0\\.0\\.1|localhost)");

    public SecurityScanService(PlatformStateStore store,WorkspaceService workspace,EnterpriseGovernanceService governance){
        this.store=store;this.workspace=workspace;this.governance=governance;
        this.history=new LinkedHashMap<>(store.read(STATE,new TypeReference<Map<UUID,List<ScanReport>>>(){},LinkedHashMap::new));
    }

    public synchronized ScanReport scan(UUID projectId,String depth,String actor){
        List<Finding> findings=new ArrayList<>();
        for(WorkspaceService.WorkspaceFile file:workspace.listFiles(projectId)){
            String content=file.content()==null?"":file.content();
            if(SECRET.matcher(content).find())findings.add(finding("SECRET_EXPOSURE","CRITICAL",file.path(),"Possible hard-coded credential or token detected.","Move the secret to project environment/secrets storage."));
            if(DANGEROUS_HTML.matcher(content).find())findings.add(finding("UNSAFE_DOM_API","HIGH",file.path(),"Potentially unsafe DOM execution API detected.","Use framework-safe rendering and sanitize untrusted content."));
            if(HTTP_URL.matcher(content).find())findings.add(finding("INSECURE_TRANSPORT","MEDIUM",file.path(),"Non-local HTTP URL detected.","Use HTTPS for external network calls."));
            if(file.path().endsWith(".sql")&&content.toLowerCase().contains("create table")&&!content.toLowerCase().contains("primary key"))findings.add(finding("DB_PRIMARY_KEY","LOW",file.path(),"Generated table appears to have no primary key.","Add a stable primary key."));
        }
        boolean blocking=findings.stream().anyMatch(f->"CRITICAL".equals(f.severity())||"HIGH".equals(f.severity()));
        ScanReport report=new ScanReport(UUID.randomUUID(),projectId,Instant.now(),actor,depth==null?"BASIC":depth.toUpperCase(),List.copyOf(findings),blocking,summary(findings));
        history.computeIfAbsent(projectId,k->new ArrayList<>()).add(0,report);store.write(STATE,history);
        governance.append("SECURITY_SCAN_COMPLETED",projectId.toString(),Map.of("scanId",report.id().toString(),"findings",findings.size(),"blocking",blocking));
        return report;
    }

    public synchronized List<ScanReport> history(UUID projectId){return List.copyOf(history.getOrDefault(projectId,List.of()));}
    public synchronized ScanReport latest(UUID projectId){return history(projectId).stream().findFirst().orElse(null);}
    public synchronized GateDecision gate(UUID projectId){ScanReport latest=latest(projectId);return latest==null?new GateDecision(false,"No security scan has been run.",null):new GateDecision(!latest.blocking(),latest.blocking()?"Publish blocked by high/critical security findings.":"Security gate passed.",latest.id());}

    public synchronized FixSuggestion suggestFix(UUID projectId,UUID findingId){
        ScanReport report=latest(projectId);if(report==null)throw new IllegalStateException("No scan available");
        Finding finding=report.findings().stream().filter(f->f.id().equals(findingId)).findFirst().orElseThrow(()->new IllegalArgumentException("Finding not found"));
        String prompt="Fix security finding "+finding.rule()+" in "+finding.file()+": "+finding.message()+" Recommended: "+finding.recommendation();
        return new FixSuggestion(finding.id(),finding.file(),prompt);
    }

    private Finding finding(String rule,String severity,String file,String message,String recommendation){return new Finding(UUID.randomUUID(),rule,severity,file,message,recommendation,"OPEN");}
    private String summary(List<Finding> findings){long c=findings.stream().filter(f->"CRITICAL".equals(f.severity())).count(),h=findings.stream().filter(f->"HIGH".equals(f.severity())).count();return findings.isEmpty()?"No findings detected.":findings.size()+" findings ("+c+" critical, "+h+" high).";}

    public record Finding(UUID id,String rule,String severity,String file,String message,String recommendation,String status){}
    public record ScanReport(UUID id,UUID projectId,Instant createdAt,String actor,String depth,List<Finding> findings,boolean blocking,String summary){}
    public record GateDecision(boolean allowed,String message,UUID scanId){}
    public record FixSuggestion(UUID findingId,String file,String prompt){}
}
