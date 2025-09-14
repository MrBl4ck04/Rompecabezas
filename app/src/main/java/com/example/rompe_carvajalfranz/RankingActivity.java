package com.example.rompe_carvajalfranz;

import android.os.Bundle;

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

        RecyclerView rv = findViewById(R.id.rv_ranking);
        rv.setLayoutManager(new LinearLayoutManager(this));

        String type = getIntent().getStringExtra("type");
        if (type == null) type = "letters";
        List<ScoreRepository.ScoreRow> data = new ScoreRepository(this).getTopScores(type, 50);
        rv.setAdapter(new RankingAdapter(data));
    }
}


