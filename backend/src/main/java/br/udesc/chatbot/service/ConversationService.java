package br.udesc.chatbot.service;

import br.udesc.chatbot.api.dto.ChatStateResponse;
import br.udesc.chatbot.api.dto.OptionDto;
import br.udesc.chatbot.api.exception.InvalidOptionException;
import br.udesc.chatbot.api.exception.SessionNotFoundException;
import br.udesc.chatbot.conversation.ConversationNode;
import br.udesc.chatbot.conversation.ConversationTreeLoader;
import br.udesc.chatbot.conversation.NodeOption;
import br.udesc.chatbot.domain.ChatSession;
import br.udesc.chatbot.domain.ChatSessionRepository;
import br.udesc.chatbot.domain.Language;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {

    private static final String ROOT_NODE = "language-select";

    private final ChatSessionRepository sessionRepository;
    private final ConversationTreeLoader treeLoader;

    public ConversationService(ChatSessionRepository sessionRepository, ConversationTreeLoader treeLoader) {
        this.sessionRepository = sessionRepository;
        this.treeLoader = treeLoader;
    }

    @Transactional
    public ChatStateResponse startSession() {
        ChatSession session = new ChatSession(UUID.randomUUID(), Language.ENGLISH, ROOT_NODE);
        sessionRepository.save(session);
        return buildResponse(session);
    }

    @Transactional(readOnly = true)
    public ChatStateResponse getSession(UUID sessionId) {
        ChatSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));
        return buildResponse(session);
    }

    @Transactional
    public ChatStateResponse selectOption(UUID sessionId, int optionIndex) {
        ChatSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));

        ConversationNode currentNode = treeLoader.getNode(session.getCurrentNodeId());
        List<NodeOption> options = currentNode.getOptions();

        if (options == null || optionIndex < 0 || optionIndex >= options.size()) {
            int max = options == null ? 0 : options.size();
            throw new InvalidOptionException(optionIndex, max);
        }

        NodeOption chosen = options.get(optionIndex);

        if (chosen.getLanguageCode() != null) {
            Language lang = Language.fromCode(chosen.getLanguageCode());
            session.setLanguage(lang);
        }

        session.setCurrentNodeId(chosen.getNextNodeId());
        session.setLastActiveAt(LocalDateTime.now());
        sessionRepository.save(session);

        return buildResponse(session);
    }

    private ChatStateResponse buildResponse(ChatSession session) {
        ConversationNode node = treeLoader.getNode(session.getCurrentNodeId());
        String langCode = session.getLanguage().getCode();

        String message = node.getMessage().getOrDefault(langCode, node.getMessage().get("en"));

        List<OptionDto> optionDtos = new ArrayList<>();
        if (node.getOptions() != null) {
            for (int i = 0; i < node.getOptions().size(); i++) {
                NodeOption opt = node.getOptions().get(i);
                String label = opt.getLabel().getOrDefault(langCode, opt.getLabel().get("en"));
                optionDtos.add(new OptionDto(i, label));
            }
        }

        return new ChatStateResponse(session.getId(), node.getId(), message, optionDtos);
    }
}
