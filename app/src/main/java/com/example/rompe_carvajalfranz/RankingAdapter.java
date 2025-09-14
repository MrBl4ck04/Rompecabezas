package com.example.rompe_carvajalfranz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.VH> {

    private final List<ScoreRepository.ScoreRow> items;

    public RankingAdapter(List<ScoreRepository.ScoreRow> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ScoreRepository.ScoreRow row = items.get(position);
        holder.user.setText(row.username);
        holder.moves.setText("Movs: " + row.moves);
        long ms = row.timeMs;
        long m = TimeUnit.MILLISECONDS.toMinutes(ms);
        long s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60;
        holder.time.setText(String.format("%02d:%02d", m, s));
        holder.rank.setText(String.valueOf(position + 1));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView rank, user, moves, time;
        VH(@NonNull View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.txt_rank);
            user = itemView.findViewById(R.id.txt_user);
            moves = itemView.findViewById(R.id.txt_moves);
            time = itemView.findViewById(R.id.txt_time);
        }
    }
}


