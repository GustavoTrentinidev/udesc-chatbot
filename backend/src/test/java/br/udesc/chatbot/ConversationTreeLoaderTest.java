package br.udesc.chatbot;

import br.udesc.chatbot.conversation.ConversationNode;
import br.udesc.chatbot.conversation.ConversationTreeLoader;
import br.udesc.chatbot.conversation.NodeOption;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ConversationTreeLoaderTest {

    @Autowired
    private ConversationTreeLoader loader;

    @Test
    void allNodesLoad() {
        Map<String, ConversationNode> nodes = loader.getAllNodes();
        assertThat(nodes).isNotEmpty();
        assertThat(nodes).containsKey("language-select");
        assertThat(nodes).containsKey("menu-main");
    }

    @Test
    void everyNodeHasTrilingualMessage() {
        loader.getAllNodes().forEach((id, node) -> {
            assertThat(node.getMessage())
                .as("Node '%s' missing EN message", id)
                .containsKey("en");
            assertThat(node.getMessage())
                .as("Node '%s' missing PT message", id)
                .containsKey("pt");
            assertThat(node.getMessage())
                .as("Node '%s' missing ES message", id)
                .containsKey("es");
        });
    }

    @Test
    void allNextNodeIdsResolveToExistingNodes() {
        Map<String, ConversationNode> nodes = loader.getAllNodes();
        nodes.forEach((id, node) -> {
            if (node.getOptions() != null) {
                for (NodeOption option : node.getOptions()) {
                    assertThat(nodes)
                        .as("Node '%s' has option pointing to unknown node '%s'", id, option.getNextNodeId())
                        .containsKey(option.getNextNodeId());
                }
            }
        });
    }
}
