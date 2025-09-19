package com.example.rompe_carvajalfranz;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AuthRepository {
    private final DBHelper dbHelper;

    public AuthRepository(Context context) {
        this.dbHelper = new DBHelper(context.getApplicationContext());
    }

    public long registerUser(String username, char[] password) throws IllegalArgumentException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("username requerido");
        }
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("password requerido");
        }

        // Verificar si el usuario ya existe
        if (getUserIdByUsername(username.trim()) != -1) {
            return -1; // Usuario ya existe
        }

        byte[] salt = SecurityUtils.generateSalt();
        byte[] hash = SecurityUtils.hashPassword(password, salt);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.UserTable.COL_USERNAME, username.trim());
        values.put(DBHelper.UserTable.COL_PASSWORD_HASH, hash);
        values.put(DBHelper.UserTable.COL_PASSWORD_SALT, salt);
        values.put(DBHelper.UserTable.COL_CREATED_AT, System.currentTimeMillis());

        return db.insert(DBHelper.UserTable.TABLE, null, values);
    }

    public long getUserIdByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DBHelper.UserTable.TABLE,
                new String[]{DBHelper.UserTable._ID},
                DBHelper.UserTable.COL_USERNAME + " = ?",
                new String[]{username}, null, null, null)) {
            if (c.moveToFirst()) {
                return c.getLong(0);
            }
            return -1;
        }
    }

    public boolean validateLogin(String username, char[] password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DBHelper.UserTable.TABLE,
                new String[]{DBHelper.UserTable.COL_PASSWORD_HASH, DBHelper.UserTable.COL_PASSWORD_SALT},
                DBHelper.UserTable.COL_USERNAME + " = ?",
                new String[]{username}, null, null, null)) {
            if (!c.moveToFirst()) return false;
            byte[] storedHash = c.getBlob(0);
            byte[] storedSalt = c.getBlob(1);
            byte[] computed = SecurityUtils.hashPassword(password, storedSalt);
            return SecurityUtils.constantTimeEquals(storedHash, computed);
        }
    }
}


