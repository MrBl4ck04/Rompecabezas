package com.example.rompe_carvajalfranz;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF = "session_prefs";
    private static final String KEY_USER = "user_name";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void setLoggedInUser(String username) {
        prefs.edit().putString(KEY_USER, username).apply();
    }

    public String getLoggedInUser() {
        return prefs.getString(KEY_USER, null);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}


