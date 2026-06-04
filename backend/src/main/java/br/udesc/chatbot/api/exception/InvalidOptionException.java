package br.udesc.chatbot.api.exception;

public class InvalidOptionException extends RuntimeException {
    public InvalidOptionException(int index, int max) {
        super("Invalid option index " + index + ". Valid range: 0–" + (max - 1));
    }
}
