package com.udesc.chatbot;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private static final String URL_MAIN_SITE = "https://www.udesc.br";
    private static final String URL_STUDENT_PORTAL = "https://www.udesc.br/cefid/portaldoaluno";
    private static final String URL_INTERNATIONAL = "https://www.udesc.br/intercambio";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnMainSite = findViewById(R.id.btnMainSite);
        Button btnStudentPortal = findViewById(R.id.btnStudentPortal);
        Button btnInternational = findViewById(R.id.btnInternational);
        Button btnStartConversation = findViewById(R.id.btnStartConversation);

        btnMainSite.setOnClickListener(v -> openUrl(URL_MAIN_SITE));
        btnStudentPortal.setOnClickListener(v -> openUrl(URL_STUDENT_PORTAL));
        btnInternational.setOnClickListener(v -> openUrl(URL_INTERNATIONAL));
        btnStartConversation.setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class)));
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
