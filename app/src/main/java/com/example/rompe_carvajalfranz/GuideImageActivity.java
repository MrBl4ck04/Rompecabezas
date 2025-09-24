package com.example.rompe_carvajalfranz;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class GuideImageActivity extends AppCompatActivity {

    private ImageView guideImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_image);

        guideImageView = findViewById(R.id.guide_image_view);

        // Obtener la imagen original del intent
        String imagePath = getIntent().getStringExtra("original_image_path");
        if (imagePath != null) {
            try {
                Bitmap originalImage = BitmapFactory.decodeFile(imagePath);
                if (originalImage != null) {
                    guideImageView.setImageBitmap(originalImage);
                } else {
                    showPlaceholder();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error al cargar la imagen guía", Toast.LENGTH_SHORT).show();
                showPlaceholder();
            }
        } else {
            showPlaceholder();
        }
    }

    private void showPlaceholder() {
        guideImageView.setImageResource(R.mipmap.ic_launcher);
        Toast.makeText(this, "Imagen guía no disponible", Toast.LENGTH_SHORT).show();
    }
}
