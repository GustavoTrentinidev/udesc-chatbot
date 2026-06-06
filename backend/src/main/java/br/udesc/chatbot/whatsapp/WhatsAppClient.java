package br.udesc.chatbot.whatsapp;

import br.udesc.chatbot.api.dto.OptionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class WhatsAppClient {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppClient.class);
    private static final int BUTTON_TITLE_MAX = 20;
    private static final int LIST_TITLE_MAX = 24;
    private static final int BODY_TEXT_MAX = 1024;

    private final RestTemplate restTemplate;
    private final WhatsAppProperties properties;

    public WhatsAppClient(RestTemplate restTemplate, WhatsAppProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public void sendButtonMessage(String to, String bodyText, List<OptionDto> options) {
        List<Map<String, Object>> buttons = new ArrayList<>();
        for (OptionDto opt : options) {
            Map<String, Object> reply = new LinkedHashMap<>();
            reply.put("id", String.valueOf(opt.index()));
            reply.put("title", truncate(opt.label(), BUTTON_TITLE_MAX));
            Map<String, Object> button = new LinkedHashMap<>();
            button.put("type", "reply");
            button.put("reply", reply);
            buttons.add(button);
        }

        Map<String, Object> action = new LinkedHashMap<>();
        action.put("buttons", buttons);

        Map<String, Object> interactive = new LinkedHashMap<>();
        interactive.put("type", "button");
        interactive.put("body", Map.of("text", truncate(bodyText, BODY_TEXT_MAX)));
        interactive.put("action", action);

        Map<String, Object> payload = buildBase(to);
        payload.put("type", "interactive");
        payload.put("interactive", interactive);

        send(payload);
    }

    public void sendListMessage(String to, String bodyText, List<OptionDto> options) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (OptionDto opt : options) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", String.valueOf(opt.index()));
            row.put("title", truncate(opt.label(), LIST_TITLE_MAX));
            rows.add(row);
        }

        Map<String, Object> section = new LinkedHashMap<>();
        section.put("title", "Options");
        section.put("rows", rows);

        Map<String, Object> action = new LinkedHashMap<>();
        action.put("button", "See options");
        action.put("sections", List.of(section));

        Map<String, Object> interactive = new LinkedHashMap<>();
        interactive.put("type", "list");
        interactive.put("body", Map.of("text", truncate(bodyText, BODY_TEXT_MAX)));
        interactive.put("action", action);

        Map<String, Object> payload = buildBase(to);
        payload.put("type", "interactive");
        payload.put("interactive", interactive);

        send(payload);
    }

    public void sendTextMessage(String to, String text) {
        Map<String, Object> payload = buildBase(to);
        payload.put("type", "text");
        payload.put("text", Map.of("body", text));
        send(payload);
    }

    public void markAsRead(String to, String messageId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("status", "read");
        payload.put("message_id", messageId);
        send(payload);
    }

    private void send(Map<String, Object> payload) {
        String url = "https://graph.facebook.com/v19.0/" + properties.getPhoneNumberId() + "/messages";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getAccessToken());
        try {
            restTemplate.postForObject(url, new HttpEntity<>(payload, headers), String.class);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message: {}", e.getMessage());
        }
    }

    private Map<String, Object> buildBase(String to) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("recipient_type", "individual");
        payload.put("to", to);
        return payload;
    }

    private String truncate(String text, int max) {
        if (text == null || text.length() <= max) return text;
        return text.substring(0, max - 1) + "…";
    }
}
