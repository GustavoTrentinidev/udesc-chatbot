package br.udesc.chatbot.twilio;

import br.udesc.chatbot.api.exception.SessionNotFoundException;
import br.udesc.chatbot.service.ConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TwilioSessionStore {

    private static final Logger log = LoggerFactory.getLogger(TwilioSessionStore.class);

    private final ConcurrentHashMap<String, UUID> phoneToSession = new ConcurrentHashMap<>();
    private final ConversationService conversationService;

    public TwilioSessionStore(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    public UUID getOrCreateSession(String phoneNumber) {
        UUID sessionId = phoneToSession.get(phoneNumber);
        if (sessionId != null) {
            try {
                conversationService.getSession(sessionId);
                return sessionId;
            } catch (SessionNotFoundException e) {
                log.info("Session {} expired for {}, creating new one", sessionId, phoneNumber);
            }
        }
        var newSession = conversationService.startSession();
        UUID newId = newSession.sessionId();
        phoneToSession.put(phoneNumber, newId);
        return newId;
    }
}
