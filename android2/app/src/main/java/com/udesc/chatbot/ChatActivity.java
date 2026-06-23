package com.udesc.chatbot;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.udesc.chatbot.model.ChatState;
import com.udesc.chatbot.model.Option;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private static final String KEY_SESSION_ID = "sessionId";

    private ScrollView scrollView;
    private LinearLayout messagesContainer;
    private LinearLayout optionsContainer;
    private Button btnRestart;
    private Button btnRetry;

    private ChatApiClient apiClient;
    private String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        scrollView = findViewById(R.id.scrollView);
        messagesContainer = findViewById(R.id.messagesContainer);
        optionsContainer = findViewById(R.id.optionsContainer);
        btnRestart = findViewById(R.id.btnRestart);
        btnRetry = findViewById(R.id.btnRetry);

        apiClient = new ChatApiClient();

        btnRestart.setOnClickListener(v -> {
            messagesContainer.removeAllViews();
            startSession();
        });
        btnRetry.setOnClickListener(v -> startSession());

        if (savedInstanceState != null) {
            sessionId = savedInstanceState.getString(KEY_SESSION_ID);
        }

        if (sessionId != null) {
            restoreSession();
        } else {
            startSession();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (sessionId != null) {
            outState.putString(KEY_SESSION_ID, sessionId);
        }
    }

    private void startSession() {
        setUiLoading();
        apiClient.startSession(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showSessionStartError());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> showSessionStartError());
                    return;
                }
                String body = response.body().string();
                ChatState state = apiClient.parse(body);
                runOnUiThread(() -> {
                    sessionId = state.sessionId;
                    renderState(state);
                });
            }
        });
    }

    private void restoreSession() {
        setUiLoading();
        apiClient.getSession(sessionId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showApiError("Could not restore session."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> showApiError("Could not restore session."));
                    return;
                }
                String body = response.body().string();
                ChatState state = apiClient.parse(body);
                runOnUiThread(() -> renderState(state));
            }
        });
    }

    private void renderState(ChatState state) {
        appendMessage(state.message);

        optionsContainer.removeAllViews();
        btnRestart.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);

        if (state.options == null || state.options.isEmpty()) {
            btnRestart.setVisibility(View.VISIBLE);
            return;
        }

        for (Option option : state.options) {
            Button btn = new Button(this);
            btn.setText(option.label);
            btn.setBackgroundTintList(getColorStateList(R.color.udesc_blue));
            btn.setTextColor(getColor(R.color.white));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = dp(6);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                appendUserMessage(option.label);
                setOptionsEnabled(false);
                apiClient.selectOption(sessionId, option.index, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> {
                            showApiError("Network error. Please try again.");
                            setOptionsEnabled(true);
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            runOnUiThread(() -> {
                                showApiError("Request failed. Please try again.");
                                setOptionsEnabled(true);
                            });
                            return;
                        }
                        String body = response.body().string();
                        ChatState next = apiClient.parse(body);
                        runOnUiThread(() -> renderState(next));
                    }
                });
            });

            optionsContainer.addView(btn);
        }
    }

    private void appendUserMessage(String text) {
        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText(text);
        tv.setTextSize(16f);
        tv.setTextColor(getColor(R.color.white));
        tv.setPadding(dp(14), dp(10), dp(14), dp(10));
        tv.setLineSpacing(0f, 1.3f);

        GradientDrawable bubble = new GradientDrawable();
        bubble.setColor(getColor(R.color.udesc_blue));
        bubble.setCornerRadius(dp(12));
        tv.setBackground(bubble);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(10);
        params.setMarginStart(dp(48));
        params.gravity = Gravity.END;
        tv.setLayoutParams(params);

        messagesContainer.addView(tv);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void appendMessage(String text) {
        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText(text);
        tv.setTextSize(16f);
        tv.setTextColor(getColor(R.color.text_primary));
        tv.setPadding(dp(14), dp(10), dp(14), dp(10));
        tv.setLineSpacing(0f, 1.3f);

        GradientDrawable bubble = new GradientDrawable();
        bubble.setColor(getColor(R.color.white));
        bubble.setCornerRadius(dp(12));
        tv.setBackground(bubble);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(10);
        params.setMarginEnd(dp(48));
        params.gravity = Gravity.START;
        tv.setLayoutParams(params);

        messagesContainer.addView(tv);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void setUiLoading() {
        optionsContainer.removeAllViews();
        btnRestart.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
    }

    private void setOptionsEnabled(boolean enabled) {
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            optionsContainer.getChildAt(i).setEnabled(enabled);
        }
    }

    private void showApiError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showSessionStartError() {
        appendMessage("Could not connect to the chatbot. Make sure the backend is running.");
        btnRetry.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Connection failed.", Toast.LENGTH_LONG).show();
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
