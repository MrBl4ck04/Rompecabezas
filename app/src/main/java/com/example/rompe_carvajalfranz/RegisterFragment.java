package com.example.rompe_carvajalfranz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RegisterFragment extends Fragment {

    interface RegisterNavigator { void backToLogin(); }

    private AuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authRepository = new AuthRepository(requireContext());

        EditText username = view.findViewById(R.id.reg_username);
        EditText password = view.findViewById(R.id.reg_password);
        EditText password2 = view.findViewById(R.id.reg_password_repeat);
        Button btnRegister = view.findViewById(R.id.btn_register);
        View backBtn = view.findViewById(R.id.back_btn);

        btnRegister.setOnClickListener(v -> {
            String u = username.getText().toString().trim();
            String p1 = password.getText().toString();
            String p2 = password2.getText().toString();
            if (u.isEmpty() || p1.isEmpty()) {
                Toast.makeText(requireContext(), "Completa usuario y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!p1.equals(p2)) {
                Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
            long id = authRepository.registerUser(u, p1.toCharArray());
            if (id == -1) {
                Toast.makeText(requireContext(), "Usuario ya existe", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Registrado", Toast.LENGTH_SHORT).show();
                if (getActivity() instanceof RegisterNavigator) {
                    ((RegisterNavigator) getActivity()).backToLogin();
                }
            }
        });

        backBtn.setOnClickListener(v -> {
            if (getActivity() instanceof RegisterNavigator) {
                ((RegisterNavigator) getActivity()).backToLogin();
            }
        });
    }
}


