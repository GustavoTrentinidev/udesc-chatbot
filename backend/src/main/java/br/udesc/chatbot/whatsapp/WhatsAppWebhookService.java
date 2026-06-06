package br.udesc.chatbot.whatsapp;

import br.udesc.chatbot.api.dto.ChatStateResponse;
import br.udesc.chatbot.api.exception.InvalidOptionException;
import br.udesc.chatbot.service.ConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WhatsAppWebhookService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookService.class);

    private final ConversationService conversationService;
    private final WhatsAppSessionStore sessionStore;
    private final WhatsAppClient whatsAppClient;
    private final MessageDeduplicator deduplicator;

    public WhatsAppWebhookService(
        ConversationService conversationService,
        WhatsAppSessionStore sessionStore,
        WhatsAppClient whatsAppClient,
        MessageDeduplicator deduplicator
    ) {
        this.conversationService = conversationService;
        this.sessionStore = sessionStore;
        this.whatsAppClient = whatsAppClient;
        this.deduplicator = deduplicator;
    }

    @Async
    public void processPayload(WhatsAppWebhookPayload payload) {
        WhatsAppWebhookPayload.Message message = extractMessage(payload);
        if (message == null) return;

        if (deduplicator.isDuplicate(message.getId())) {
            log.debug("Duplicate message {} ignored", message.getId());
            return;
        }

        whatsAppClient.markAsRead(message.getFrom(), message.getId());

        UUID sessionId = sessionStore.getOrCreateSession(message.getFrom());

        ChatStateResponse response = resolveResponse(message, sessionId);
        if (response != null) {
            sendResponse(message.getFrom(), response);
        }
    }

    private ChatStateResponse resolveResponse(WhatsAppWebhookPayload.Message message, UUID sessionId) {
        String type = message.getType();

        if ("interactive".equals(type) && message.getInteractive() != null) {
            String optionIdStr = extractInteractiveId(message.getInteractive());
            if (optionIdStr != null) {
                try {
                    int optionIndex = Integer.parseInt(optionIdStr);
                    return conversationService.selectOption(sessionId, optionIndex);
                } catch (NumberFormatException | InvalidOptionException e) {
                    log.warn("Invalid option from WhatsApp: {}", optionIdStr);
                    return conversationService.getSession(sessionId);
                }
            }
        }

        return conversationService.getSession(sessionId);
    }

    private void sendResponse(String to, ChatStateResponse response) {
        List<?> options = response.options();
        int count = options == null ? 0 : options.size();

        if (count == 0) {
            whatsAppClient.sendTextMessage(to, response.message());
        } else if (count == 1) {
            String text = response.message() + "\n\n" + response.options().get(0).label();
            whatsAppClient.sendTextMessage(to, text);
        } else if (count <= 3) {
            whatsAppClient.sendButtonMessage(to, response.message(), response.options());
        } else {
            whatsAppClient.sendListMessage(to, response.message(), response.options());
        }
    }

    private WhatsAppWebhookPayload.Message extractMessage(WhatsAppWebhookPayload payload) {
        if (payload.getEntry() == null || payload.getEntry().isEmpty()) return null;
        var changes = payload.getEntry().get(0).getChanges();
        if (changes == null || changes.isEmpty()) return null;
        var messages = changes.get(0).getValue().getMessages();
        if (messages == null || messages.isEmpty()) return null;
        return messages.get(0);
    }

    private String extractInteractiveId(WhatsAppWebhookPayload.Interactive interactive) {
        if ("button_reply".equals(interactive.getType()) && interactive.getButtonReply() != null) {
            return interactive.getButtonReply().getId();
        }
        if ("list_reply".equals(interactive.getType()) && interactive.getListReply() != null) {
            return interactive.getListReply().getId();
        }
        return null;
    }
}
