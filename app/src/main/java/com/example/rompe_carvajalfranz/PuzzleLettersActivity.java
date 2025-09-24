package com.example.rompe_carvajalfranz;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PuzzleLettersActivity extends AppCompatActivity {

    private GridLayout grid;
    private Chronometer chrono;
    private int gridSize = 3; // 3x3
    private int emptyIndex; // index of empty tile
    private int moves;
    private final java.util.List<Integer> state = new java.util.ArrayList<>();
    private final java.util.List<Integer> goalState = new java.util.ArrayList<>();
    private boolean chronoStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_letters);
        
        try {
            grid = findViewById(R.id.grid);
            chrono = findViewById(R.id.chrono);

            if (grid == null || chrono == null) {
                Toast.makeText(this, "Error al cargar la interfaz", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            grid.setColumnCount(gridSize);
            grid.setRowCount(gridSize);

            setupBoard();

            Button btnSolve = findViewById(R.id.btn_solve);
            if (btnSolve != null) {
                btnSolve.setOnClickListener(v -> solveCurrent());
            }

            Button btnSize = findViewById(R.id.btn_size);
            if (btnSize != null) {
                btnSize.setOnClickListener(v -> chooseSize());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al inicializar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupBoard() {
        // Asegurar configuración del grid según tamaño actual
        grid.setColumnCount(gridSize);
        grid.setRowCount(gridSize);

        // Estado objetivo letras, 0 vacío
        goalState.clear();
        for (int i = 0; i < gridSize * gridSize - 1; i++) goalState.add(i + 1);
        goalState.add(0);

        // Crear barajado resoluble como copia del objetivo
        List<Integer> shuffled = new ArrayList<>(goalState);
        do {
            Collections.shuffle(shuffled);
        } while (!isSolvableInts(shuffled));

        grid.removeAllViews();
        state.clear();
        int index = 0;
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                int value = shuffled.get(index);
                TextView tv = new TextView(this);
                tv.setText(value == 0 ? "" : String.valueOf((char) ('A' + (value - 1))));
                // Ajustar tamaño de fuente según tamaño de grilla
                float textSp = (gridSize <= 2) ? 28 : (gridSize == 3 ? 22 : (gridSize == 4 ? 18 : 16));
                tv.setTextSize(textSp);
                tv.setGravity(android.view.Gravity.CENTER);
                tv.setTextColor(getResources().getColor(R.color.purple_700, getTheme()));
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
                
                if (value == 0) {
                    tv.setBackground(ContextCompat.getDrawable(this, R.drawable.empty_tile_background));
                } else {
                    tv.setBackground(ContextCompat.getDrawable(this, R.drawable.tile_background_enhanced));
                }
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.width = 0;
                lp.height = 0;
                lp.columnSpec = GridLayout.spec(c, 1f);
                lp.rowSpec = GridLayout.spec(r, 1f);
                lp.setMargins(4, 4, 4, 4);
                tv.setLayoutParams(lp);
                tv.setOnClickListener(v -> onTileClickFromView(v));
                tv.setTag(index);
                grid.addView(tv);
                if (value == 0) emptyIndex = index;
                state.add(value);
                index++;
            }
        }

        if (state.size() != gridSize * gridSize) {
            Toast.makeText(this, "Error al inicializar el estado del rompecabezas", Toast.LENGTH_SHORT).show();
            return;
        }

        moves = 0;
        chrono.stop();
        chrono.setBase(SystemClock.elapsedRealtime());
        chrono.start();
        grid.requestLayout();
    }

    private void onTileClick(int tileIndex) {
        try {
            // Validar índices
            if (tileIndex < 0 || tileIndex >= gridSize * gridSize || 
                emptyIndex < 0 || emptyIndex >= gridSize * gridSize) {
                return;
            }
            
            // No permitir click en la casilla vacía
            if (tileIndex == emptyIndex) {
                return;
            }
            
            // Verificar si el movimiento es válido
            if (canSwap(tileIndex, emptyIndex)) {
                if (!chronoStarted) { chrono.setBase(SystemClock.elapsedRealtime()); chrono.start(); chronoStarted = true; }
                swapTiles(tileIndex, emptyIndex);
                emptyIndex = tileIndex;
                moves++;
                
                // Verificar si se completó
                if (isCompleted()) {
                    chrono.stop();
                    long elapsed = SystemClock.elapsedRealtime() - chrono.getBase();
                    String user = new SessionManager(this).getLoggedInUser();
                    if (user == null || user.trim().isEmpty()) user = "guest";
                    new ScoreRepository(this).insertScore(user, "letters", gridSize, moves, elapsed);
                    showCompletionDialog(elapsed, moves);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error en movimiento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onTileClickFromView(View v) {
        Object tag = v.getTag();
        if (!(tag instanceof Integer)) return;
        int tileIndex = (Integer) tag;
        onTileClick(tileIndex);
    }

    private boolean canSwap(int a, int b) {
        int ar = a / gridSize, ac = a % gridSize;
        int br = b / gridSize, bc = b % gridSize;
        return (ar == br && Math.abs(ac - bc) == 1) || (ac == bc && Math.abs(ar - br) == 1);
    }

    private void swapTiles(int a, int b) {
        try {
            if (a < 0 || a >= state.size() || b < 0 || b >= state.size()) {
                return;
            }
            int tmp = state.get(a);
            state.set(a, state.get(b));
            state.set(b, tmp);

            View va = grid.getChildAt(a);
            View vb = grid.getChildAt(b);
            if (va instanceof TextView) { applyValueToCell((TextView) va, state.get(a)); animateCell(va); }
            if (vb instanceof TextView) { applyValueToCell((TextView) vb, state.get(b)); animateCell(vb); }
        } catch (Exception e) {
            Toast.makeText(this, "Error al intercambiar fichas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void applyValueToCell(TextView cell, int value) {
        if (value == 0) {
            cell.setText("");
            cell.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        } else {
            cell.setText(String.valueOf((char) ('A' + (value - 1))));
            cell.setBackground(ContextCompat.getDrawable(this, R.drawable.tile_background));
        }
    }

    private void animateCell(View v) {
        try {
            v.animate()
                    .setDuration(120)
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .alpha(0.9f)
                    .withEndAction(() -> v.animate()
                            .setDuration(120)
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .start())
                    .start();
        } catch (Exception ignored) {}
    }

    private boolean isCompleted() {
        try {
            if (state.size() != goalState.size()) return false;
            for (int i = 0; i < state.size(); i++) {
                if (!state.get(i).equals(goalState.get(i))) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void solveCurrent() {
        try {
            if (state.size() != gridSize * gridSize) {
                Toast.makeText(this, "Estado del rompecabezas inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int[] arr = new int[state.size()];
            for (int i = 0; i < state.size(); i++) arr[i] = state.get(i);
            java.util.List<Integer> movesIdx = FifteenPuzzleSolver.solve(arr);
            
            if (movesIdx == null || movesIdx.isEmpty()) {
                Toast.makeText(this, "No se encontró solución", Toast.LENGTH_SHORT).show();
                return;
            }
            
            playSolution(movesIdx);
        } catch (Exception e) {
            Toast.makeText(this, "Error al resolver: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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

    private void showCompletionDialog(long elapsedMs, int moves) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMs);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMs) % 60;
        
        String message = String.format("¡Felicitaciones!\n\nTiempo: %02d:%02d\nMovimientos: %d", 
                minutes, seconds, moves);
        
        new AlertDialog.Builder(this)
                .setTitle("¡Rompecabezas Completado!")
                .setMessage(message)
                .setPositiveButton("Jugar de nuevo", (dialog, which) -> {
                    setupBoard();
                })
                .setNegativeButton("Menú principal", (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    // Solvabilidad basada en permutación 1..8 ignorando 0
    private boolean isSolvableInts(List<Integer> arr) {
        List<Integer> perm = new ArrayList<>();
        for (Integer v : arr) if (v != 0) perm.add(v);
        int inversions = 0;
        for (int i = 0; i < perm.size(); i++) {
            for (int j = i + 1; j < perm.size(); j++) {
                if (perm.get(i) > perm.get(j)) inversions++;
            }
        }
        int emptyPos = arr.indexOf(0);
        int emptyRowFromBottom = gridSize - (emptyPos / gridSize);
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

    private void chooseSize() {
        final String[] labels = {"2 x 2", "3 x 3", "4 x 4", "5 x 5"};
        final int[] sizes = {2, 3, 4, 5};
        new AlertDialog.Builder(this)
                .setTitle("Selecciona tamaño")
                .setItems(labels, (dialog, which) -> {
                    if (which < 0 || which >= sizes.length) return;
                    gridSize = sizes[which];
                    grid.setColumnCount(gridSize);
                    grid.setRowCount(gridSize);
                    setupBoard();
                })
                .show();
    }
}


