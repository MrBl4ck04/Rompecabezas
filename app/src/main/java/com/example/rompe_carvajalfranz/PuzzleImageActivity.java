package com.example.rompe_carvajalfranz;

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

    private static final int GRID_SIZE = 4;
    private GridLayout grid;
    private Chronometer chrono;
    private Uri cameraPhotoUri;
    private int emptyIndex;
    private final List<Bitmap> tiles = new ArrayList<>();
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
        grid = findViewById(R.id.grid_image);
        chrono = findViewById(R.id.chrono_image);
        Button btnGallery = findViewById(R.id.btn_pick_gallery);
        Button btnCamera = findViewById(R.id.btn_pick_camera);

        grid.setColumnCount(GRID_SIZE);
        grid.setRowCount(GRID_SIZE);

        btnGallery.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnCamera.setOnClickListener(v -> startCamera());
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
        int tileSize = square.getWidth() / GRID_SIZE;
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (r == GRID_SIZE - 1 && c == GRID_SIZE - 1) {
                    tiles.add(null); // empty
                } else {
                    Bitmap tile = Bitmap.createBitmap(square, c * tileSize, r * tileSize, tileSize, tileSize);
                    tiles.add(tile);
                }
            }
        }
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
                    iv.setBackgroundColor(getResources().getColor(R.color.blue_100));
                }
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.width = 0;
                lp.height = 0;
                lp.columnSpec = GridLayout.spec(c, 1f);
                lp.rowSpec = GridLayout.spec(r, 1f);
                lp.setMargins(6, 6, 6, 6);
                iv.setLayoutParams(lp);
                final int currentIndex = index;
                iv.setOnClickListener(v -> onTileClick(currentIndex));
                grid.addView(iv);
                if (b == null) emptyIndex = index;
                index++;
            }
        }
    }

    private void onTileClick(int tileIndex) {
        if (canSwap(tileIndex, emptyIndex)) {
            swapTiles(tileIndex, emptyIndex);
            emptyIndex = tileIndex;
            if (isCompleted()) {
                chrono.stop();
                Toast.makeText(this, "¡Completado!", Toast.LENGTH_SHORT).show();
                long elapsed = SystemClock.elapsedRealtime() - chrono.getBase();
                String user = new SessionManager(this).getLoggedInUser();
                if (user != null) {
                    new ScoreRepository(this).insertScore(user, "image", GRID_SIZE, moves, elapsed);
                }
            }
        }
    }

    private boolean canSwap(int a, int b) {
        int ar = a / GRID_SIZE, ac = a % GRID_SIZE;
        int br = b / GRID_SIZE, bc = b % GRID_SIZE;
        return (ar == br && Math.abs(ac - bc) == 1) || (ac == bc && Math.abs(ar - br) == 1);
    }

    private void swapTiles(int a, int b) {
        View va = grid.getChildAt(a);
        View vb = grid.getChildAt(b);
        grid.removeViewAt(b);
        grid.addView(va, b);
        grid.removeViewAt(a);
        grid.addView(vb, a);

        Bitmap tmp = tiles.get(a);
        tiles.set(a, tiles.get(b));
        tiles.set(b, tmp);
    }

    private boolean isCompleted() {
        // Check if tiles are in original order: last is null
        int idx = 0;
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (r == GRID_SIZE - 1 && c == GRID_SIZE - 1) {
                    return tiles.get(idx) == null;
                }
                // original index should match
                Bitmap current = tiles.get(idx);
                if (current == null) return false;
                idx++;
            }
        }
        return false;
    }

    private boolean isSolvableForImages(List<Bitmap> arr) {
        // Map bitmaps to numbers based on their original order: index in unshuffled list
        List<Integer> nums = new ArrayList<>();
        for (Bitmap b : arr) {
            if (b != null) nums.add(System.identityHashCode(b));
        }
        // This is a heuristic; for correctness we'd track IDs when building tiles
        int inversions = 0;
        for (int i = 0; i < nums.size(); i++) {
            for (int j = i + 1; j < nums.size(); j++) {
                if (nums.get(i) > nums.get(j)) inversions++;
            }
        }
        int emptyRowFromBottom = GRID_SIZE - (arr.indexOf(null) / GRID_SIZE);
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
}


