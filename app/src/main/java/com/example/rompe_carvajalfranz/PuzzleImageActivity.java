package com.example.rompe_carvajalfranz;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import java.util.concurrent.TimeUnit;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PuzzleImageActivity extends AppCompatActivity {

    private static final int GRID_SIZE = 3;
    private GridLayout grid;
    private Chronometer chrono;
    private Uri cameraPhotoUri;
    private int emptyIndex;
    private final List<Bitmap> tiles = new ArrayList<>();
    // Estado objetivo estable (orden correcto de las piezas)
    private final List<Bitmap> goalTiles = new ArrayList<>();
    private int moves;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null) {
                        onImageChosen(result);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean success) {
                    if (Boolean.TRUE.equals(success) && cameraPhotoUri != null) {
                        onImageChosen(cameraPhotoUri);
                    } else {
                        Toast.makeText(PuzzleImageActivity.this, "Foto cancelada", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_image);
        
        try {
            grid = findViewById(R.id.grid_image);
            chrono = findViewById(R.id.chrono_image);
            Button btnGallery = findViewById(R.id.btn_pick_gallery);
            Button btnCamera = findViewById(R.id.btn_pick_camera);
            Button btnSolve = findViewById(R.id.btn_solve_image);

            if (grid == null || chrono == null || btnGallery == null || btnCamera == null || btnSolve == null) {
                Toast.makeText(this, "Error al cargar la interfaz", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            grid.setColumnCount(GRID_SIZE);
            grid.setRowCount(GRID_SIZE);

            btnGallery.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
            btnCamera.setOnClickListener(v -> startCamera());
            btnSolve.setOnClickListener(v -> solveCurrent());
        } catch (Exception e) {
            Toast.makeText(this, "Error al inicializar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startCamera() {
        try {
            cameraPhotoUri = createImageUri();
            if (cameraPhotoUri != null) {
                takePictureLauncher.launch(cameraPhotoUri);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri createImageUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";
        if (Build.VERSION.SDK_INT >= 29) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Rompecabezas");
            return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            File imagesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (imagesDir == null) return null;
            File image = new File(imagesDir, fileName);
            return FileProvider.getUriForFile(this, getPackageName() + ".provider", image);
        }
    }

    private void onImageChosen(Uri uri) {
        Bitmap bitmap = loadBitmapCorrected(uri);
        if (bitmap == null) {
            Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap square = cropCenterSquare(bitmap);
        buildTiles(square);
        setupBoard();
        moves = 0;
        chrono.setBase(SystemClock.elapsedRealtime());
        chrono.start();
    }

    @Nullable
    private Bitmap loadBitmapCorrected(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) return null;
            Bitmap bmp = BitmapFactory.decodeStream(is);
            is.close();
            // Try EXIF orientation
            InputStream eis = getContentResolver().openInputStream(uri);
            if (eis != null) {
                ExifInterface exif = new ExifInterface(eis);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                eis.close();
                return applyOrientation(bmp, orientation);
            }
            return bmp;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private Bitmap applyOrientation(Bitmap src, int orientation) {
        Matrix m = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                m.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                m.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                m.postRotate(270);
                break;
            default:
                return src;
        }
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, true);
    }

    private Bitmap cropCenterSquare(Bitmap bmp) {
        int size = Math.min(bmp.getWidth(), bmp.getHeight());
        int x = (bmp.getWidth() - size) / 2;
        int y = (bmp.getHeight() - size) / 2;
        return Bitmap.createBitmap(bmp, x, y, size, size);
    }

    private void buildTiles(Bitmap square) {
        tiles.clear();
        goalTiles.clear();
        int tileSize = square.getWidth() / GRID_SIZE;
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (r == GRID_SIZE - 1 && c == GRID_SIZE - 1) {
                    goalTiles.add(null); // vacío al final
                } else {
                    Bitmap tile = Bitmap.createBitmap(square, c * tileSize, r * tileSize, tileSize, tileSize);
                    goalTiles.add(tile);
                }
            }
        }
        // Crear una copia barajada del objetivo
        tiles.addAll(goalTiles);
        do {
            Collections.shuffle(tiles);
        } while (!isSolvableForImages(tiles));
    }

    private void setupBoard() {
        grid.removeAllViews();
        int index = 0;
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Bitmap b = tiles.get(index);
                ImageView iv = new ImageView(this);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (b != null) {
                    iv.setImageBitmap(b);
                    iv.setBackground(ContextCompat.getDrawable(this, R.drawable.tile_background));
                }
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.width = 0;
                lp.height = 0;
                lp.columnSpec = GridLayout.spec(c, 1f);
                lp.rowSpec = GridLayout.spec(r, 1f);
                lp.setMargins(6, 6, 6, 6);
                iv.setLayoutParams(lp);
                // Guardar el índice lógico actual en el tag del view
                iv.setTag(index);
                iv.setOnClickListener(v -> onTileClickFromView(v));
                grid.addView(iv);
                if (b == null) emptyIndex = index;
                index++;
            }
        }
        
        // Validar que el estado se inicializó correctamente
        if (tiles.size() != GRID_SIZE * GRID_SIZE) {
            Toast.makeText(this, "Error al inicializar el estado del rompecabezas", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void onTileClick(int tileIndex) {
        try {
            // Validar índices
            if (tileIndex < 0 || tileIndex >= GRID_SIZE * GRID_SIZE || 
                emptyIndex < 0 || emptyIndex >= GRID_SIZE * GRID_SIZE) {
                return;
            }
            
            // No permitir click en la casilla vacía
            if (tileIndex == emptyIndex) {
                return;
            }
            
            // Verificar si el movimiento es válido
            if (canSwap(tileIndex, emptyIndex)) {
                swapTiles(tileIndex, emptyIndex);
                emptyIndex = tileIndex;
                moves++;
                
                // Verificar si se completó
                if (isCompleted()) {
                    chrono.stop();
                    long elapsed = SystemClock.elapsedRealtime() - chrono.getBase();
                    String user = new SessionManager(this).getLoggedInUser();
                    if (user != null) {
                        new ScoreRepository(this).insertScore(user, "image", GRID_SIZE, moves, elapsed);
                    }
                    showCompletionDialog(elapsed, moves);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error en movimiento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Obtiene el índice lógico desde la vista clicada usando su tag
    private void onTileClickFromView(View v) {
        Object tag = v.getTag();
        if (!(tag instanceof Integer)) return;
        int tileIndex = (Integer) tag;
        onTileClick(tileIndex);
    }

    private boolean canSwap(int a, int b) {
        int ar = a / GRID_SIZE, ac = a % GRID_SIZE;
        int br = b / GRID_SIZE, bc = b % GRID_SIZE;
        return (ar == br && Math.abs(ac - bc) == 1) || (ac == bc && Math.abs(ar - br) == 1);
    }

    private void swapTiles(int a, int b) {
        try {
            if (a < 0 || a >= tiles.size() || b < 0 || b >= tiles.size()) {
                return;
            }

            // Intercambiar bitmaps en el estado lógico
            Bitmap tmp = tiles.get(a);
            tiles.set(a, tiles.get(b));
            tiles.set(b, tmp);

            // Actualizar visualmente solo las dos celdas afectadas
            View va = grid.getChildAt(a);
            View vb = grid.getChildAt(b);
            if (va instanceof ImageView) {
                applyBitmapToCell((ImageView) va, tiles.get(a));
            }
            if (vb instanceof ImageView) {
                applyBitmapToCell((ImageView) vb, tiles.get(b));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al intercambiar fichas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void applyBitmapToCell(ImageView cell, @Nullable Bitmap bmp) {
        if (bmp == null) {
            cell.setImageDrawable(null);
            cell.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        } else {
            cell.setImageBitmap(bmp);
            cell.setBackground(ContextCompat.getDrawable(this, R.drawable.tile_background));
        }
    }

    private void solveCurrent() {
        try {
            if (tiles.size() != GRID_SIZE * GRID_SIZE || goalTiles.size() != GRID_SIZE * GRID_SIZE) {
                Toast.makeText(this, "Estado inválido para resolver", Toast.LENGTH_SHORT).show();
                return;
            }
            // Mapear estado actual a enteros 1..8, 0 vacío según orden objetivo
            int[] arr = new int[tiles.size()];
            for (int i = 0; i < tiles.size(); i++) {
                Bitmap b = tiles.get(i);
                if (b == null) arr[i] = 0; else arr[i] = goalTiles.indexOf(b) + 1;
            }
            List<Integer> movesIdx = FifteenPuzzleSolver.solve(arr);
            if (movesIdx == null || movesIdx.isEmpty()) {
                Toast.makeText(this, "No se encontró solución", Toast.LENGTH_SHORT).show();
                return;
            }
            playSolution(movesIdx);
        } catch (Exception e) {
            Toast.makeText(this, "Error al resolver: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void playSolution(List<Integer> movesIdx) {
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

    private boolean isCompleted() {
        try {
            if (tiles.size() != GRID_SIZE * GRID_SIZE || goalTiles.size() != GRID_SIZE * GRID_SIZE) {
                return false;
            }
            for (int i = 0; i < goalTiles.size(); i++) {
                if (tiles.get(i) != goalTiles.get(i)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isSolvableForImages(List<Bitmap> arr) {
        // Calcular permutación respecto al estado objetivo usando índices de goalTiles
        List<Integer> perm = new ArrayList<>();
        for (Bitmap b : arr) {
            if (b == null) continue; // ignorar vacío
            int idx = goalTiles.indexOf(b);
            // idx estará entre 0..7 para piezas
            perm.add(idx + 1); // usar 1..8 para piezas
        }
        int inversions = 0;
        for (int i = 0; i < perm.size(); i++) {
            for (int j = i + 1; j < perm.size(); j++) {
                if (perm.get(i) > perm.get(j)) inversions++;
            }
        }
        int emptyPos = arr.indexOf(null);
        int emptyRowFromBottom = GRID_SIZE - (emptyPos / GRID_SIZE);
        if (GRID_SIZE % 2 == 1) {
            return inversions % 2 == 0;
        } else {
            if (emptyRowFromBottom % 2 == 0) {
                return inversions % 2 == 1;
            } else {
                return inversions % 2 == 0;
            }
        }
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
                    // Volver a seleccionar imagen
                    pickImageLauncher.launch("image/*");
                })
                .setNegativeButton("Menú principal", (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}


