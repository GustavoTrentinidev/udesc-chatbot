package br.udesc.chatbot.whatsapp;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class MessageDeduplicator {

    private static final int MAX_SIZE = 500;

    private final Set<String> seen = Collections.synchronizedSet(
        Collections.newSetFromMap(
            new LinkedHashMap<>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                    return size() > MAX_SIZE;
                }
            }
        )
    );

    public boolean isDuplicate(String messageId) {
        return !seen.add(messageId);
    }
}
