package com.example.rompe_carvajalfranz;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authRepository = new AuthRepository(this);

        EditText username = findViewById(R.id.reg_username);
        EditText password = findViewById(R.id.reg_password);
        EditText password2 = findViewById(R.id.reg_password_repeat);
        Button btnRegister = findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(v -> {
            String u = username.getText().toString().trim();
            String p1 = password.getText().toString();
            String p2 = password2.getText().toString();
            if (u.isEmpty() || p1.isEmpty()) {
                Toast.makeText(this, "Completa usuario y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!p1.equals(p2)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
            long id = authRepository.registerUser(u, p1.toCharArray());
            if (id == -1) {
                Toast.makeText(this, "Usuario ya existe", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Registrado, vuelve al login", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}


