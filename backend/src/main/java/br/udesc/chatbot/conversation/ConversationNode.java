package br.udesc.chatbot.conversation;

import java.util.List;
import java.util.Map;

public class ConversationNode {
    private String id;
    private Map<String, String> message;
    private List<NodeOption> options;
    private String backNodeId;

    public ConversationNode() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Map<String, String> getMessage() { return message; }
    public void setMessage(Map<String, String> message) { this.message = message; }
    public List<NodeOption> getOptions() { return options; }
    public void setOptions(List<NodeOption> options) { this.options = options; }
    public String getBackNodeId() { return backNodeId; }
    public void setBackNodeId(String backNodeId) { this.backNodeId = backNodeId; }

    public boolean isLeaf() {
        return options == null || options.isEmpty();
    }
}
