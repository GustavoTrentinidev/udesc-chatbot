package br.udesc.chatbot.whatsapp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageDeduplicatorTest {

    private final MessageDeduplicator deduplicator = new MessageDeduplicator();

    @Test
    void firstCallReturnsFalse() {
        assertThat(deduplicator.isDuplicate("msg-001")).isFalse();
    }

    @Test
    void secondCallWithSameIdReturnsTrue() {
        deduplicator.isDuplicate("msg-002");
        assertThat(deduplicator.isDuplicate("msg-002")).isTrue();
    }

    @Test
    void differentIdsAreNotDuplicates() {
        deduplicator.isDuplicate("msg-003");
        assertThat(deduplicator.isDuplicate("msg-004")).isFalse();
    }

    @Test
    void capAt500EntriesEvictsOldest() {
        for (int i = 0; i < 501; i++) {
            deduplicator.isDuplicate("cap-msg-" + i);
        }
        // msg-0 should have been evicted (500 cap, 501 added)
        assertThat(deduplicator.isDuplicate("cap-msg-0")).isFalse();
        // msg-500 (most recent) should still be a duplicate
        assertThat(deduplicator.isDuplicate("cap-msg-500")).isTrue();
    }
}
