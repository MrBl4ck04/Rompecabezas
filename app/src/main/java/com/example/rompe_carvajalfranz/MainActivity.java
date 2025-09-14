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

        findViewById(R.id.btn_letters).setOnClickListener(v -> {
            startActivity(new Intent(this, PuzzleLettersActivity.class));
        });

        findViewById(R.id.btn_image).setOnClickListener(v -> {
            startActivity(new Intent(this, PuzzleImageActivity.class));
        });

        findViewById(R.id.btn_ranking).setOnClickListener(v -> {
            Intent i = new Intent(this, RankingActivity.class);
            i.putExtra("type", "letters");
            startActivity(i);
        });
    }
}