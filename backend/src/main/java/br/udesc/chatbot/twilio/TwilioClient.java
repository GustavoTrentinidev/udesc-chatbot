package br.udesc.chatbot.twilio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class TwilioClient {

    private static final Logger log = LoggerFactory.getLogger(TwilioClient.class);

    private final RestTemplate restTemplate;
    private final TwilioProperties properties;

    public TwilioClient(RestTemplate restTemplate, TwilioProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public void sendMessage(String to, String body) {
        String url = "https://api.twilio.com/2010-04-01/Accounts/" + properties.getAccountSid() + "/Messages.json";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(properties.getAccountSid(), properties.getAuthToken());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("From", properties.getFromNumber());
        params.add("To", to);
        params.add("Body", body);

        try {
            restTemplate.postForObject(url, new HttpEntity<>(params, headers), String.class);
        } catch (Exception e) {
            log.error("Failed to send Twilio message: {}", e.getMessage());
        }
    }
}
