package com.example.rompe_carvajalfranz;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AuthActivity extends AppCompatActivity implements LoginFragment.LoginNavigator, RegisterFragment.RegisterNavigator {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_container, new LoginFragment())
                    .commit();
        }
    }

    @Override
    public void goToRegister() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.auth_container, new RegisterFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void backToLogin() {
        getSupportFragmentManager().popBackStack();
    }
}


