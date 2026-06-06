package br.udesc.chatbot.whatsapp;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "whatsapp")
@Validated
public class WhatsAppProperties {

    @NotBlank(message = "whatsapp.phone-number-id must be set (WHATSAPP_PHONE_NUMBER_ID env var)")
    private String phoneNumberId;

    @NotBlank(message = "whatsapp.access-token must be set (WHATSAPP_ACCESS_TOKEN env var)")
    private String accessToken;

    @NotBlank(message = "whatsapp.verify-token must be set (WHATSAPP_VERIFY_TOKEN env var)")
    private String verifyToken;

    public String getPhoneNumberId() { return phoneNumberId; }
    public void setPhoneNumberId(String phoneNumberId) { this.phoneNumberId = phoneNumberId; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getVerifyToken() { return verifyToken; }
    public void setVerifyToken(String verifyToken) { this.verifyToken = verifyToken; }
}
