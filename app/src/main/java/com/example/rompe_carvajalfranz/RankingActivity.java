package com.example.rompe_carvajalfranz;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RankingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        try {
            RecyclerView rv = findViewById(R.id.rv_ranking);
            if (rv == null) {
                Toast.makeText(this, "Error: No se pudo cargar el ranking", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            rv.setLayoutManager(new LinearLayoutManager(this));

            String type = getIntent().getStringExtra("type");
            if (type == null || (!"letters".equals(type) && !"image".equals(type))) type = "letters";
            
            ScoreRepository scoreRepo = new ScoreRepository(this);
            List<ScoreRepository.ScoreRow> data = scoreRepo.getTopScores(type, 50);
            
            if (data == null || data.isEmpty()) {
                Toast.makeText(this, "No hay puntajes registrados aún", Toast.LENGTH_SHORT).show();
            }
            
            rv.setAdapter(new RankingAdapter(data));
            
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar ranking", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}


