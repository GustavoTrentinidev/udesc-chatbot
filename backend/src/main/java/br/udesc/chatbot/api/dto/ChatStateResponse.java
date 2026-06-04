package br.udesc.chatbot.api.dto;

import java.util.List;
import java.util.UUID;

public record ChatStateResponse(
    UUID sessionId,
    String nodeId,
    String message,
    List<OptionDto> options
) {}
