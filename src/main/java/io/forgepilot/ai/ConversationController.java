package io.forgepilot.ai;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Read API for the creator-first project conversation.
 *
 * @author Tejas Shah
 */
@RestController
@RequestMapping("/api/projects/{projectId}/conversation")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public List<ConversationService.ConversationMessage> messages(@PathVariable UUID projectId) {
        return conversationService.messages(projectId);
    }
}
