package com.udesc.chatbot;

import com.google.gson.Gson;
import com.udesc.chatbot.model.ChatState;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ChatApiClient {

    private static final String BASE_URL = "http://10.0.2.2:8080";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public void startSession(Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/sessions")
                .post(RequestBody.create("", JSON))
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void selectOption(String sessionId, int optionIndex, Callback callback) {
        String body = gson.toJson(new SelectRequest(optionIndex));
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/sessions/" + sessionId + "/select")
                .post(RequestBody.create(body, JSON))
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void getSession(String sessionId, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/sessions/" + sessionId)
                .get()
                .build();
        client.newCall(request).enqueue(callback);
    }

    public ChatState parse(String json) {
        return gson.fromJson(json, ChatState.class);
    }

    private static class SelectRequest {
        int optionIndex;
        SelectRequest(int optionIndex) { this.optionIndex = optionIndex; }
    }
}
