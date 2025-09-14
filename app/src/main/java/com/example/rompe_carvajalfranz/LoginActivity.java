package com.example.rompe_carvajalfranz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authRepository = new AuthRepository(this);

        EditText username = findViewById(R.id.input_username);
        EditText password = findViewById(R.id.input_password);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnGoRegister = findViewById(R.id.btn_go_register);

        btnLogin.setOnClickListener(v -> {
            String u = username.getText().toString();
            char[] p = password.getText().toString().toCharArray();
            boolean ok = authRepository.validateLogin(u, p);
            if (ok) {
                Toast.makeText(this, "Bienvenido " + u, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Credenciales inválidas", Toast.LENGTH_SHORT).show();
            }
        });

        btnGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}


