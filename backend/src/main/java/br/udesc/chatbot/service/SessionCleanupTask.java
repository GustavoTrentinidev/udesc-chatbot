package br.udesc.chatbot.service;

import br.udesc.chatbot.domain.ChatSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class SessionCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(SessionCleanupTask.class);

    private final ChatSessionRepository sessionRepository;

    public SessionCleanupTask(ChatSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void deleteIdleSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        int deleted = sessionRepository.deleteByLastActiveAtBefore(cutoff);
        if (deleted > 0) {
            log.info("Cleaned up {} idle sessions older than 24h", deleted);
        }
    }
}
