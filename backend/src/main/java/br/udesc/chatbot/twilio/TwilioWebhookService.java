package br.udesc.chatbot.twilio;

import br.udesc.chatbot.api.dto.ChatStateResponse;
import br.udesc.chatbot.api.exception.InvalidOptionException;
import br.udesc.chatbot.service.ConversationService;
import br.udesc.chatbot.twilio.MessageDeduplicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TwilioWebhookService {

    private static final Logger log = LoggerFactory.getLogger(TwilioWebhookService.class);

    private final ConversationService conversationService;
    private final TwilioSessionStore sessionStore;
    private final TwilioClient twilioClient;
    private final MessageDeduplicator deduplicator;

    public TwilioWebhookService(
        ConversationService conversationService,
        TwilioSessionStore sessionStore,
        TwilioClient twilioClient,
        MessageDeduplicator deduplicator
    ) {
        this.conversationService = conversationService;
        this.sessionStore = sessionStore;
        this.twilioClient = twilioClient;
        this.deduplicator = deduplicator;
    }

    @Async
    public void process(String from, String body, String messageSid) {
        if (deduplicator.isDuplicate(messageSid)) {
            log.debug("Duplicate message {} ignored", messageSid);
            return;
        }

        UUID sessionId = sessionStore.getOrCreateSession(from);

        ChatStateResponse response = resolveResponse(body.trim(), sessionId);
        twilioClient.sendMessage(from, formatResponse(response));
    }

    private ChatStateResponse resolveResponse(String body, UUID sessionId) {
        try {
            int index = Integer.parseInt(body);
            return conversationService.selectOption(sessionId, index);
        } catch (NumberFormatException | InvalidOptionException e) {
            return conversationService.getSession(sessionId);
        }
    }

    private String formatResponse(ChatStateResponse response) {
        if (response.options() == null || response.options().isEmpty()) {
            return response.message();
        }
        StringBuilder sb = new StringBuilder(response.message());
        sb.append("\n");
        for (var opt : response.options()) {
            sb.append("\n").append(opt.index()).append(". ").append(opt.label());
        }
        return sb.toString();
    }
}
