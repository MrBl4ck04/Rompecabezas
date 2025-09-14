package com.example.rompe_carvajalfranz;

import android.content.Intent;
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

public class LoginFragment extends Fragment {

    interface LoginNavigator { void goToRegister(); }

    private AuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authRepository = new AuthRepository(requireContext());

        EditText username = view.findViewById(R.id.input_username);
        EditText password = view.findViewById(R.id.input_password);
        Button btnLogin = view.findViewById(R.id.btn_login);
        Button btnGoRegister = view.findViewById(R.id.btn_go_register);

        btnLogin.setOnClickListener(v -> {
            String u = username.getText().toString();
            char[] p = password.getText().toString().toCharArray();
            boolean ok = authRepository.validateLogin(u, p);
            if (ok) {
                Toast.makeText(requireContext(), "Bienvenido " + u, Toast.LENGTH_SHORT).show();
                new SessionManager(requireContext()).setLoggedInUser(u);
                startActivity(new Intent(requireContext(), MainActivity.class));
                requireActivity().finish();
            } else {
                Toast.makeText(requireContext(), "Credenciales inválidas", Toast.LENGTH_SHORT).show();
            }
        });

        btnGoRegister.setOnClickListener(v -> {
            if (getActivity() instanceof LoginNavigator) {
                ((LoginNavigator) getActivity()).goToRegister();
            }
        });
    }
}


