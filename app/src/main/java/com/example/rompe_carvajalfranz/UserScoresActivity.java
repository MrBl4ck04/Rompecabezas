package com.example.rompe_carvajalfranz;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UserScoresActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_scores);
        try {
            ListView list = findViewById(R.id.list_user_scores);
            if (list == null) {
                Toast.makeText(this, "No se pudo cargar la lista", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            String username = new SessionManager(this).getLoggedInUser();
            if (username == null || username.trim().isEmpty()) username = "guest";

            String type = getIntent().getStringExtra("type"); // opcional: letters|image o null
            List<ScoreRepository.ScoreRow> rows = new ScoreRepository(this).getUserScores(username, type, 200);
            if (rows == null) rows = new ArrayList<>();

            // Ordenar mejor->peor: primero por tiempo asc, luego movimientos asc
            Collections.sort(rows, new Comparator<ScoreRepository.ScoreRow>() {
                @Override
                public int compare(ScoreRepository.ScoreRow a, ScoreRepository.ScoreRow b) {
                    int ct = Long.compare(a.timeMs, b.timeMs);
                    if (ct != 0) return ct;
                    return Integer.compare(a.moves, b.moves);
                }
            });

            ArrayList<String> items = new ArrayList<>(rows.size());
            for (ScoreRepository.ScoreRow r : rows) {
                long m = TimeUnit.MILLISECONDS.toMinutes(r.timeMs);
                long s = TimeUnit.MILLISECONDS.toSeconds(r.timeMs) % 60;
                items.add(String.format("%s  |  %02d:%02d  |  Movs: %d", r.username != null ? r.username : username, m, s, r.moves));
            }

            if (items.isEmpty()) {
                items.add("Sin puntajes todavía");
            }

            list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
        } catch (Throwable e) {
            Toast.makeText(this, "Error al abrir tus puntajes", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
