package br.udesc.chatbot.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppWebhookPayload {

    private String object;
    private List<Entry> entry;

    public String getObject() { return object; }
    public void setObject(String object) { this.object = object; }
    public List<Entry> getEntry() { return entry; }
    public void setEntry(List<Entry> entry) { this.entry = entry; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {
        private String id;
        private List<Change> changes;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public List<Change> getChanges() { return changes; }
        public void setChanges(List<Change> changes) { this.changes = changes; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Change {
        private Value value;
        private String field;

        public Value getValue() { return value; }
        public void setValue(Value value) { this.value = value; }
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        private List<Message> messages;

        public List<Message> getMessages() { return messages; }
        public void setMessages(List<Message> messages) { this.messages = messages; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String from;
        private String id;
        private String type;
        private TextBody text;
        private Interactive interactive;

        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public TextBody getText() { return text; }
        public void setText(TextBody text) { this.text = text; }
        public Interactive getInteractive() { return interactive; }
        public void setInteractive(Interactive interactive) { this.interactive = interactive; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextBody {
        private String body;

        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Interactive {
        private String type;

        @JsonProperty("button_reply")
        private ButtonReply buttonReply;

        @JsonProperty("list_reply")
        private ListReply listReply;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public ButtonReply getButtonReply() { return buttonReply; }
        public void setButtonReply(ButtonReply buttonReply) { this.buttonReply = buttonReply; }
        public ListReply getListReply() { return listReply; }
        public void setListReply(ListReply listReply) { this.listReply = listReply; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ButtonReply {
        private String id;
        private String title;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ListReply {
        private String id;
        private String title;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
}
