package br.udesc.chatbot.conversation;

import java.util.Map;

public class NodeOption {
    private Map<String, String> label;
    private String nextNodeId;
    private String languageCode;

    public NodeOption() {}

    public Map<String, String> getLabel() { return label; }
    public void setLabel(Map<String, String> label) { this.label = label; }
    public String getNextNodeId() { return nextNodeId; }
    public void setNextNodeId(String nextNodeId) { this.nextNodeId = nextNodeId; }
    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
}
