package br.udesc.chatbot.conversation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ConversationTreeLoader {

    private static final Logger log = LoggerFactory.getLogger(ConversationTreeLoader.class);

    private Map<String, ConversationNode> nodeMap;

    @PostConstruct
    public void load() {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();
            ClassPathResource resource = new ClassPathResource("conversation-tree.yaml");
            Map<String, List<ConversationNode>> wrapper = mapper.readValue(
                resource.getInputStream(),
                new TypeReference<>() {}
            );
            List<ConversationNode> nodes = wrapper.get("nodes");
            if (nodes == null || nodes.isEmpty()) {
                throw new IllegalStateException("conversation-tree.yaml has no nodes");
            }
            nodeMap = nodes.stream().collect(Collectors.toMap(ConversationNode::getId, n -> n));
            validateTree();
            log.info("Conversation tree loaded: {} nodes", nodeMap.size());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load conversation-tree.yaml", e);
        }
    }

    public ConversationNode getNode(String nodeId) {
        ConversationNode node = nodeMap.get(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("Node not found: " + nodeId);
        }
        return node;
    }

    public Map<String, ConversationNode> getAllNodes() {
        return nodeMap;
    }

    private void validateTree() {
        for (ConversationNode node : nodeMap.values()) {
            if (node.getOptions() != null) {
                for (NodeOption option : node.getOptions()) {
                    if (!nodeMap.containsKey(option.getNextNodeId())) {
                        throw new IllegalStateException(
                            "Node '" + node.getId() + "' references unknown nextNodeId: " + option.getNextNodeId()
                        );
                    }
                }
            }
            if (node.getBackNodeId() != null && !nodeMap.containsKey(node.getBackNodeId())) {
                throw new IllegalStateException(
                    "Node '" + node.getId() + "' references unknown backNodeId: " + node.getBackNodeId()
                );
            }
        }
    }
}
