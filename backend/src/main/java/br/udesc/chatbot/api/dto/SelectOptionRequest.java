package br.udesc.chatbot.api.dto;

import jakarta.validation.constraints.Min;

public record SelectOptionRequest(@Min(0) int optionIndex) {}
