package br.udesc.chatbot.whatsapp;

import br.udesc.chatbot.api.dto.ChatStateResponse;
import br.udesc.chatbot.api.exception.SessionNotFoundException;
import br.udesc.chatbot.service.ConversationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppSessionStoreTest {

    @Mock
    private ConversationService conversationService;

    private WhatsAppSessionStore store;

    @BeforeEach
    void setUp() {
        store = new WhatsAppSessionStore(conversationService);
    }

    @Test
    void newPhoneCreatesSession() {
        UUID newId = UUID.randomUUID();
        when(conversationService.startSession())
            .thenReturn(new ChatStateResponse(newId, "language-select", "Welcome", List.of()));

        UUID result = store.getOrCreateSession("+5547999999999");

        assertThat(result).isEqualTo(newId);
        verify(conversationService).startSession();
    }

    @Test
    void existingPhoneReusesSession() {
        UUID existingId = UUID.randomUUID();
        when(conversationService.startSession())
            .thenReturn(new ChatStateResponse(existingId, "language-select", "Welcome", List.of()));
        when(conversationService.getSession(existingId))
            .thenReturn(new ChatStateResponse(existingId, "menu-main", "Menu", List.of()));

        store.getOrCreateSession("+5547888888888");
        UUID second = store.getOrCreateSession("+5547888888888");

        assertThat(second).isEqualTo(existingId);
        verify(conversationService, times(1)).startSession();
    }

    @Test
    void staleSessionIsReplaced() {
        UUID expiredId = UUID.randomUUID();
        UUID newId = UUID.randomUUID();

        when(conversationService.startSession())
            .thenReturn(new ChatStateResponse(expiredId, "language-select", "Welcome", List.of()))
            .thenReturn(new ChatStateResponse(newId, "language-select", "Welcome", List.of()));
        when(conversationService.getSession(expiredId))
            .thenThrow(new SessionNotFoundException(expiredId));

        store.getOrCreateSession("+5547777777777");
        UUID result = store.getOrCreateSession("+5547777777777");

        assertThat(result).isEqualTo(newId);
        verify(conversationService, times(2)).startSession();
    }
}
