package com.example.rompe_carvajalfranz;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "puzzle.db";
    public static final int DATABASE_VERSION = 2; // subir versión para forzar onUpgrade en instalaciones previas

    public static final class UserTable implements BaseColumns {
        public static final String TABLE = "users";
        public static final String COL_USERNAME = "username"; // unique
        public static final String COL_PASSWORD_HASH = "password_hash";
        public static final String COL_PASSWORD_SALT = "password_salt";
        public static final String COL_CREATED_AT = "created_at";
    }

    public static final class ScoreTable implements BaseColumns {
        public static final String TABLE = "scores";
        public static final String COL_USER_ID = "user_id"; // FK to users._id
        public static final String COL_PUZZLE_TYPE = "puzzle_type"; // letters|image
        public static final String COL_GRID_SIZE = "grid_size"; // e.g., 4 for 4x4
        public static final String COL_MOVES = "moves";
        public static final String COL_TIME_MS = "time_ms";
        public static final String COL_CREATED_AT = "created_at";
    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTablesIfNeeded(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migración idempotente: garantizar tablas y constraints
        createTablesIfNeeded(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Asegurar que las tablas existan también en aperturas de DB antiguas
        createTablesIfNeeded(db);
    }

    private void createTablesIfNeeded(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + UserTable.TABLE + " (" +
                UserTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UserTable.COL_USERNAME + " TEXT NOT NULL UNIQUE, " +
                UserTable.COL_PASSWORD_HASH + " BLOB NOT NULL, " +
                UserTable.COL_PASSWORD_SALT + " BLOB NOT NULL, " +
                UserTable.COL_CREATED_AT + " INTEGER NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + ScoreTable.TABLE + " (" +
                ScoreTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ScoreTable.COL_USER_ID + " INTEGER NOT NULL, " +
                ScoreTable.COL_PUZZLE_TYPE + " TEXT NOT NULL, " +
                ScoreTable.COL_GRID_SIZE + " INTEGER NOT NULL, " +
                ScoreTable.COL_MOVES + " INTEGER NOT NULL, " +
                ScoreTable.COL_TIME_MS + " INTEGER NOT NULL, " +
                ScoreTable.COL_CREATED_AT + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + ScoreTable.COL_USER_ID + ") REFERENCES " + UserTable.TABLE + "(" + UserTable._ID + ") ON DELETE CASCADE" +
                ")");
    }
}


