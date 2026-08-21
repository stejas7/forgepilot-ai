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
 * Project security and quality scanner for generated applications.
 * Deterministic checks keep the publish gate reproducible and auditable.
 */
@Service
public class SecurityScanService {
    private static final String STATE="security-scans.json";
    private final PlatformStateStore store;
    private final WorkspaceService workspace;
    private final EnterpriseGovernanceService governance;
    private final Map<UUID,List<ScanReport>> history;

    private static final Pattern SECRET=Pattern.compile("(?i)(api[_-]?key|client[_-]?secret|secret|token|password)\\s*[:=]\\s*[\\\"']?[A-Za-z0-9_\\-./+=]{12,}");
    private static final Pattern DANGEROUS_HTML=Pattern.compile("(?i)(innerHTML\\s*=|outerHTML\\s*=|document\\.write\\s*\\(|eval\\s*\\(|new\\s+Function\\s*\\()");
    private static final Pattern HTTP_URL=Pattern.compile("http://(?!127\\.0\\.0\\.1|localhost)");
    private static final Pattern TOKEN_STORAGE=Pattern.compile("(?i)localStorage\\.(setItem|getItem)\\s*\\(\\s*[\\\"'](token|access[_-]?token|refresh[_-]?token|jwt)");
    private static final Pattern WILDCARD_CORS=Pattern.compile("(?i)(Access-Control-Allow-Origin[^\\n]*\\*|allowedOrigins?\\s*[:=(]\\s*[\\\"']?\\*)");
    private static final Pattern TARGET_BLANK=Pattern.compile("(?i)target=[\\\"']_blank[\\\"']");
    private static final Pattern NOOPENER=Pattern.compile("(?i)rel=[\\\"'][^\\\"']*(noopener|noreferrer)");
    private static final Pattern IMG=Pattern.compile("(?i)<img\\b[^>]*>");
    private static final Pattern ALT=Pattern.compile("(?i)\\balt\\s*=");

    public SecurityScanService(PlatformStateStore store,WorkspaceService workspace,EnterpriseGovernanceService governance){
        this.store=store;this.workspace=workspace;this.governance=governance;
        this.history=new LinkedHashMap<>(store.read(STATE,new TypeReference<Map<UUID,List<ScanReport>>>(){},LinkedHashMap::new));
    }

    public synchronized ScanReport scan(UUID projectId,String depth,String actor){
        String normalizedDepth=depth==null?"BASIC":depth.toUpperCase(Locale.ROOT);
        List<Finding> findings=new ArrayList<>();
        for(WorkspaceService.WorkspaceFile file:workspace.listFiles(projectId)){
            String content=file.content()==null?"":file.content();
            scanSecurity(file.path(),content,findings);
            scanQuality(file.path(),content,findings,normalizedDepth);
        }
        findings=deduplicate(findings);
        boolean blocking=findings.stream().anyMatch(f->"CRITICAL".equals(f.severity())||"HIGH".equals(f.severity()));
        ScanReport report=new ScanReport(UUID.randomUUID(),projectId,Instant.now(),actor,normalizedDepth,List.copyOf(findings),blocking,summary(findings));
        history.computeIfAbsent(projectId,k->new ArrayList<>()).add(0,report);
        trimHistory(projectId);persist();
        governance.append("SECURITY_SCAN_COMPLETED",projectId.toString(),Map.of("scanId",report.id().toString(),"depth",normalizedDepth,"findings",findings.size(),"blocking",blocking));
        return report;
    }

    private void scanSecurity(String path,String content,List<Finding> findings){
        if(SECRET.matcher(content).find())findings.add(finding("SECRET_EXPOSURE","CRITICAL",path,"Possible hard-coded credential or token detected.","Move the value to project secrets/environment storage."));
        if(DANGEROUS_HTML.matcher(content).find())findings.add(finding("UNSAFE_DOM_API","HIGH",path,"Potentially unsafe DOM/code execution API detected.","Use framework-safe rendering and sanitize untrusted content."));
        if(TOKEN_STORAGE.matcher(content).find())findings.add(finding("TOKEN_IN_LOCAL_STORAGE","HIGH",path,"Authentication token appears to be stored in localStorage.","Prefer secure httpOnly same-site cookies or a hardened session strategy."));
        if(WILDCARD_CORS.matcher(content).find())findings.add(finding("WILDCARD_CORS","HIGH",path,"Wildcard CORS policy detected.","Restrict allowed origins to explicit trusted application origins."));
        if(HTTP_URL.matcher(content).find())findings.add(finding("INSECURE_TRANSPORT","MEDIUM",path,"Non-local HTTP URL detected.","Use HTTPS for external network calls."));
        if(path.endsWith(".sql")&&content.toLowerCase(Locale.ROOT).contains("create table")&&!content.toLowerCase(Locale.ROOT).contains("primary key"))findings.add(finding("DB_PRIMARY_KEY","LOW",path,"Generated table appears to have no primary key.","Add a stable primary key."));
        if(path.endsWith(".sql")&&content.toLowerCase(Locale.ROOT).contains("grant all"))findings.add(finding("DB_OVER_PRIVILEGE","HIGH",path,"Broad database GRANT ALL permission detected.","Grant only the minimum privileges required by the generated application."));
    }

    private void scanQuality(String path,String content,List<Finding> findings,String depth){
        String lower=content.toLowerCase(Locale.ROOT);
        if((path.endsWith(".html")||path.endsWith(".tsx")||path.endsWith(".jsx"))&&TARGET_BLANK.matcher(content).find()&&!NOOPENER.matcher(content).find())findings.add(finding("EXTERNAL_LINK_OPENER","LOW",path,"A target=_blank link may expose window.opener.","Add rel=\"noopener noreferrer\" to external links."));
        if(path.endsWith(".html")&&lower.contains("<html")&&!lower.matches("(?s).*<html[^>]*\\blang\\s*=.*"))findings.add(finding("A11Y_HTML_LANG","LOW",path,"HTML document has no language attribute.","Add a valid lang attribute to the html element."));
        if(path.endsWith(".html")){
            var matcher=IMG.matcher(content);while(matcher.find()){if(!ALT.matcher(matcher.group()).find()){findings.add(finding("A11Y_IMAGE_ALT","LOW",path,"Image element is missing alternative text.","Add meaningful alt text, or alt=\"\" for decorative images."));break;}}
        }
        if("DEEP".equals(depth)&&path.endsWith("package.json")&&(content.contains("\": \"latest\"")||content.contains("\":\"latest\"")))findings.add(finding("UNPINNED_DEPENDENCY","MEDIUM",path,"A dependency uses the floating 'latest' version.","Pin dependencies to reviewed versions and update them deliberately."));
        if("DEEP".equals(depth)&&(path.endsWith(".js")||path.endsWith(".ts")||path.endsWith(".tsx"))&&lower.contains("console.log("))findings.add(finding("DEBUG_LOGGING","INFO",path,"Debug logging remains in generated application code.","Remove or route logs through an intentional application logger before production."));
    }

    public synchronized List<ScanReport> history(UUID projectId){return List.copyOf(history.getOrDefault(projectId,List.of()));}
    public synchronized ScanReport latest(UUID projectId){return history(projectId).stream().findFirst().orElse(null);}
    public synchronized GateDecision gate(UUID projectId){ScanReport latest=latest(projectId);return latest==null?new GateDecision(false,"No security scan has been run.",null):new GateDecision(!latest.blocking(),latest.blocking()?"Publish blocked by high/critical security findings.":"Security gate passed.",latest.id());}

    public synchronized FixSuggestion suggestFix(UUID projectId,UUID findingId){
        ScanReport report=latest(projectId);if(report==null)throw new IllegalStateException("No scan available");
        Finding finding=report.findings().stream().filter(f->f.id().equals(findingId)).findFirst().orElseThrow(()->new IllegalArgumentException("Finding not found"));
        String prompt="Fix security/quality finding "+finding.rule()+" in "+finding.file()+": "+finding.message()+" Recommended remediation: "+finding.recommendation()+" Preserve existing behavior, then verify the fix and rerun security checks.";
        return new FixSuggestion(finding.id(),finding.file(),prompt);
    }

    public synchronized Finding resolve(UUID projectId,UUID findingId,String actor){
        ScanReport latest=latest(projectId);if(latest==null)throw new IllegalStateException("No scan available");
        Finding target=latest.findings().stream().filter(f->f.id().equals(findingId)).findFirst().orElseThrow(()->new IllegalArgumentException("Finding not found"));
        Finding resolved=new Finding(target.id(),target.rule(),target.severity(),target.file(),target.message(),target.recommendation(),"RESOLVED");
        List<Finding> updated=latest.findings().stream().map(f->f.id().equals(findingId)?resolved:f).toList();
        boolean blocking=updated.stream().anyMatch(f->"OPEN".equals(f.status())&&("CRITICAL".equals(f.severity())||"HIGH".equals(f.severity())));
        ScanReport report=new ScanReport(latest.id(),latest.projectId(),latest.createdAt(),latest.actor(),latest.depth(),updated,blocking,summary(updated));
        history.get(projectId).set(0,report);persist();governance.append("SECURITY_FINDING_RESOLVED",projectId.toString(),Map.of("findingId",findingId.toString(),"actor",actor));return resolved;
    }

    public synchronized TrustSummary trust(UUID projectId){
        ScanReport latest=latest(projectId);if(latest==null)return new TrustSummary(projectId,"NOT_SCANNED",null,0,0,0,0,false,List.of("Run a security scan before publishing."));
        long critical=count(latest,"CRITICAL"),high=count(latest,"HIGH"),medium=count(latest,"MEDIUM"),low=count(latest,"LOW");
        String grade=critical>0?"CRITICAL":high>0?"REVIEW_REQUIRED":medium>0?"ATTENTION":"HEALTHY";
        List<String> controls=new ArrayList<>();controls.add("Immutable publish snapshots");controls.add("Approval-aware publishing");controls.add("Security publish gate");controls.add("Audit events");
        return new TrustSummary(projectId,grade,latest.createdAt(),critical,high,medium,low,!latest.blocking(),List.copyOf(controls));
    }

    public synchronized WorkspaceSecuritySummary workspaceSummary(){
        long scanned=history.values().stream().filter(list->!list.isEmpty()).count();
        long blocked=history.values().stream().filter(list->!list.isEmpty()&&list.get(0).blocking()).count();
        long critical=history.values().stream().filter(list->!list.isEmpty()).map(list->list.get(0)).mapToLong(r->count(r,"CRITICAL")).sum();
        long high=history.values().stream().filter(list->!list.isEmpty()).map(list->list.get(0)).mapToLong(r->count(r,"HIGH")).sum();
        return new WorkspaceSecuritySummary(scanned,blocked,critical,high,scanned-blocked,Instant.now());
    }

    private long count(ScanReport report,String severity){return report.findings().stream().filter(f->"OPEN".equals(f.status())&&severity.equals(f.severity())).count();}
    private Finding finding(String rule,String severity,String file,String message,String recommendation){return new Finding(UUID.randomUUID(),rule,severity,file,message,recommendation,"OPEN");}
    private List<Finding> deduplicate(List<Finding> input){Map<String,Finding> unique=new LinkedHashMap<>();for(Finding f:input)unique.putIfAbsent(f.rule()+"|"+f.file(),f);return new ArrayList<>(unique.values());}
    private String summary(List<Finding> findings){long c=findings.stream().filter(f->"OPEN".equals(f.status())&&"CRITICAL".equals(f.severity())).count(),h=findings.stream().filter(f->"OPEN".equals(f.status())&&"HIGH".equals(f.severity())).count();long open=findings.stream().filter(f->"OPEN".equals(f.status())).count();return open==0?"No open findings detected.":open+" open findings ("+c+" critical, "+h+" high).";}
    private void trimHistory(UUID projectId){List<ScanReport> reports=history.get(projectId);if(reports!=null&&reports.size()>50)reports.subList(50,reports.size()).clear();}
    private void persist(){store.write(STATE,history);}

    public record Finding(UUID id,String rule,String severity,String file,String message,String recommendation,String status){}
    public record ScanReport(UUID id,UUID projectId,Instant createdAt,String actor,String depth,List<Finding> findings,boolean blocking,String summary){}
    public record GateDecision(boolean allowed,String message,UUID scanId){}
    public record FixSuggestion(UUID findingId,String file,String prompt){}
    public record TrustSummary(UUID projectId,String status,Instant lastScan,long critical,long high,long medium,long low,boolean publishGatePassed,List<String> implementedControls){}
    public record WorkspaceSecuritySummary(long scannedProjects,long blockedProjects,long criticalFindings,long highFindings,long passingProjects,Instant calculatedAt){}
}
