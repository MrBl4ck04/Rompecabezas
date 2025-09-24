package com.example.rompe_carvajalfranz;

import android.os.Bundle;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.card_letters).setOnClickListener(v -> {
            startActivity(new Intent(this, PuzzleLettersActivity.class));
        });

        findViewById(R.id.card_image).setOnClickListener(v -> {
            startActivity(new Intent(this, PuzzleImageActivity.class));
        });

        findViewById(R.id.card_user_scores).setOnClickListener(v -> {
            startActivity(new Intent(this, UserScoresActivity.class));
        });

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            new SessionManager(this).clear();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });
    }
}