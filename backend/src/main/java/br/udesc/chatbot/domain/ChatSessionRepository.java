package br.udesc.chatbot.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    @Modifying
    @Query("DELETE FROM ChatSession s WHERE s.lastActiveAt < :cutoff")
    int deleteByLastActiveAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
