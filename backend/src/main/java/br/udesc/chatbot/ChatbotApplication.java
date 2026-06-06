package br.udesc.chatbot;

import br.udesc.chatbot.twilio.TwilioProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties(TwilioProperties.class)
public class ChatbotApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatbotApplication.class, args);
    }
}
