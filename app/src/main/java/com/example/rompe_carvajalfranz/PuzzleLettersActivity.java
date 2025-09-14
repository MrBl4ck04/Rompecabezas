package com.example.rompe_carvajalfranz;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PuzzleLettersActivity extends AppCompatActivity {

    private GridLayout grid;
    private Chronometer chrono;
    private int gridSize = 4; // 4x4
    private int emptyIndex; // index of empty tile
    private int moves;
    private final java.util.List<Integer> state = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_letters);
        grid = findViewById(R.id.grid);
        chrono = findViewById(R.id.chrono);

        grid.setColumnCount(gridSize);
        grid.setRowCount(gridSize);

        setupBoard();

        Button btnSolve = findViewById(R.id.btn_solve);
        if (btnSolve != null) {
            btnSolve.setOnClickListener(v -> solveCurrent());
        }
    }

    private void setupBoard() {
        List<String> tiles = new ArrayList<>();
        for (int i = 0; i < gridSize * gridSize - 1; i++) {
            tiles.add(String.valueOf((char) ('A' + i))); // A..O for 15 tiles
        }
        tiles.add(""); // empty tile

        do {
            Collections.shuffle(tiles);
        } while (!isSolvable(tiles));

        grid.removeAllViews();
        state.clear();
        int index = 0;
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                String label = tiles.get(index);
                TextView tv = new TextView(this);
                tv.setText(label);
                tv.setTextSize(20);
                tv.setGravity(android.view.Gravity.CENTER);
                tv.setBackgroundColor(getResources().getColor(label.isEmpty() ? android.R.color.transparent : R.color.blue_200));
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.width = 0;
                lp.height = 0;
                lp.columnSpec = GridLayout.spec(c, 1f);
                lp.rowSpec = GridLayout.spec(r, 1f);
                lp.setMargins(6, 6, 6, 6);
                tv.setLayoutParams(lp);
                final int currentIndex = index;
                tv.setOnClickListener(v -> onTileClick(currentIndex));
                grid.addView(tv);
                if (label.isEmpty()) emptyIndex = index;
                state.add(label.isEmpty() ? 0 : (label.charAt(0) - 'A' + 1));
                index++;
            }
        }

        moves = 0;
        chrono.setBase(SystemClock.elapsedRealtime());
        chrono.start();
    }

    private void onTileClick(int tileIndex) {
        if (canSwap(tileIndex, emptyIndex)) {
            swapTiles(tileIndex, emptyIndex);
            emptyIndex = tileIndex;
            moves++;
            if (isCompleted()) {
                chrono.stop();
                long elapsed = SystemClock.elapsedRealtime() - chrono.getBase();
                String user = new SessionManager(this).getLoggedInUser();
                if (user != null) {
                    new ScoreRepository(this).insertScore(user, "letters", gridSize, moves, elapsed);
                }
            }
        }
    }

    private boolean canSwap(int a, int b) {
        int ar = a / gridSize, ac = a % gridSize;
        int br = b / gridSize, bc = b % gridSize;
        return (ar == br && Math.abs(ac - bc) == 1) || (ac == bc && Math.abs(ar - br) == 1);
    }

    private void swapTiles(int a, int b) {
        View va = grid.getChildAt(a);
        View vb = grid.getChildAt(b);
        grid.removeViewAt(b);
        grid.addView(va, b);
        grid.removeViewAt(a);
        grid.addView(vb, a);
        int tmp = state.get(a);
        state.set(a, state.get(b));
        state.set(b, tmp);
    }

    private boolean isCompleted() {
        for (int i = 0; i < gridSize * gridSize - 1; i++) {
            TextView tv = (TextView) grid.getChildAt(i);
            String expected = String.valueOf((char) ('A' + i));
            if (!expected.contentEquals(tv.getText())) return false;
        }
        TextView last = (TextView) grid.getChildAt(gridSize * gridSize - 1);
        return last.getText().length() == 0;
    }

    private void solveCurrent() {
        int[] arr = new int[state.size()];
        for (int i = 0; i < state.size(); i++) arr[i] = state.get(i);
        java.util.List<Integer> movesIdx = FifteenPuzzleSolver.solve(arr);
        playSolution(movesIdx);
    }

    private void playSolution(java.util.List<Integer> movesIdx) {
        if (movesIdx == null || movesIdx.isEmpty()) return;
        chrono.stop();
        final int[] step = {0};
        grid.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (step[0] >= movesIdx.size()) {
                    return;
                }
                int target = movesIdx.get(step[0]);
                if (canSwap(target, emptyIndex)) {
                    swapTiles(target, emptyIndex);
                    emptyIndex = target;
                }
                step[0]++;
                grid.postDelayed(this, 180);
            }
        }, 200);
    }

    // Classic inversion count method for 15-puzzle solvability
    private boolean isSolvable(List<String> tiles) {
        int inversions = 0;
        List<Integer> nums = new ArrayList<>();
        for (String s : tiles) {
            if (!s.isEmpty()) nums.add((int) s.charAt(0));
        }
        for (int i = 0; i < nums.size(); i++) {
            for (int j = i + 1; j < nums.size(); j++) {
                if (nums.get(i) > nums.get(j)) inversions++;
            }
        }
        int emptyRowFromBottom = gridSize - (tiles.indexOf("") / gridSize);
        if (gridSize % 2 == 1) {
            return inversions % 2 == 0;
        } else {
            if (emptyRowFromBottom % 2 == 0) {
                return inversions % 2 == 1;
            } else {
                return inversions % 2 == 0;
            }
        }
    }
}


