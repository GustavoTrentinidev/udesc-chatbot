package br.udesc.chatbot;

import br.udesc.chatbot.api.dto.ChatStateResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void postSessionReturns201WithLanguageSelect() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/sessions"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.sessionId").isNotEmpty())
            .andExpect(jsonPath("$.nodeId").value("language-select"))
            .andExpect(jsonPath("$.options.length()").value(3))
            .andReturn();

        ChatStateResponse response = objectMapper.readValue(
            result.getResponse().getContentAsString(), ChatStateResponse.class
        );
        assertThat(response.sessionId()).isNotNull();
    }

    @Test
    void getSessionForUnknownIdReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/sessions/00000000-0000-0000-0000-000000000000"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void selectValidOptionReturnsNextNode() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/sessions"))
            .andExpect(status().isCreated())
            .andReturn();
        ChatStateResponse session = objectMapper.readValue(
            createResult.getResponse().getContentAsString(), ChatStateResponse.class
        );

        mockMvc.perform(post("/api/v1/sessions/{id}/select", session.sessionId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"optionIndex\": 0}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nodeId").value("menu-main"));
    }

    @Test
    void selectInvalidOptionReturns400() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/sessions"))
            .andExpect(status().isCreated())
            .andReturn();
        ChatStateResponse session = objectMapper.readValue(
            createResult.getResponse().getContentAsString(), ChatStateResponse.class
        );

        mockMvc.perform(post("/api/v1/sessions/{id}/select", session.sessionId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"optionIndex\": 99}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").isNotEmpty())
            .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}
