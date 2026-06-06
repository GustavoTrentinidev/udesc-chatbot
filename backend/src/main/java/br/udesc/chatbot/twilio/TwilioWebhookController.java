package br.udesc.chatbot.twilio;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook/twilio")
public class TwilioWebhookController {

    private final TwilioWebhookService webhookService;

    public TwilioWebhookController(TwilioWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> receive(
        @RequestParam("From") String from,
        @RequestParam("Body") String body,
        @RequestParam("MessageSid") String messageSid
    ) {
        webhookService.process(from, body, messageSid);
        return ResponseEntity.ok().build();
    }
}
