package br.udesc.chatbot.api;

import br.udesc.chatbot.api.dto.ChatStateResponse;
import br.udesc.chatbot.api.dto.SelectOptionRequest;
import br.udesc.chatbot.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
public class ChatController {

    private final ConversationService conversationService;

    public ChatController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatStateResponse startSession() {
        return conversationService.startSession();
    }

    @GetMapping("/{sessionId}")
    public ChatStateResponse getSession(@PathVariable UUID sessionId) {
        return conversationService.getSession(sessionId);
    }

    @PostMapping("/{sessionId}/select")
    public ChatStateResponse selectOption(
        @PathVariable UUID sessionId,
        @Valid @RequestBody SelectOptionRequest request
    ) {
        return conversationService.selectOption(sessionId, request.optionIndex());
    }
}
