package br.udesc.chatbot;

import br.udesc.chatbot.api.dto.ChatStateResponse;
import br.udesc.chatbot.api.exception.InvalidOptionException;
import br.udesc.chatbot.service.ConversationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class ConversationServiceTest {

    @Autowired
    private ConversationService conversationService;

    @Test
    void sessionCreationStartsAtLanguageSelect() {
        ChatStateResponse response = conversationService.startSession();
        assertThat(response.sessionId()).isNotNull();
        assertThat(response.nodeId()).isEqualTo("language-select");
        assertThat(response.options()).hasSize(3);
    }

    @Test
    void selectingEnglishNavigatesToMainMenuInEnglish() {
        ChatStateResponse session = conversationService.startSession();
        // Option 0 = English
        ChatStateResponse next = conversationService.selectOption(session.sessionId(), 0);
        assertThat(next.nodeId()).isEqualTo("menu-main");
        assertThat(next.message()).contains("Main Menu");
    }

    @Test
    void selectingPortugueseNavigatesToMainMenuInPortuguese() {
        ChatStateResponse session = conversationService.startSession();
        // Option 1 = Portuguese
        ChatStateResponse next = conversationService.selectOption(session.sessionId(), 1);
        assertThat(next.nodeId()).isEqualTo("menu-main");
        assertThat(next.message()).contains("Menu Principal");
    }

    @Test
    void selectingSpanishNavigatesToMainMenuInSpanish() {
        ChatStateResponse session = conversationService.startSession();
        // Option 2 = Spanish
        ChatStateResponse next = conversationService.selectOption(session.sessionId(), 2);
        assertThat(next.nodeId()).isEqualTo("menu-main");
        assertThat(next.message()).contains("Menú Principal");
    }

    @Test
    void invalidOptionIndexThrowsException() {
        ChatStateResponse session = conversationService.startSession();
        assertThatThrownBy(() -> conversationService.selectOption(session.sessionId(), 99))
            .isInstanceOf(InvalidOptionException.class);
    }
}
