package com.example.rompe_carvajalfranz;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ScoreRepository {
    private final DBHelper dbHelper;
    private final AuthRepository authRepository;

    public ScoreRepository(Context context) {
        this.dbHelper = new DBHelper(context.getApplicationContext());
        this.authRepository = new AuthRepository(context.getApplicationContext());
    }

    public long insertScore(String username, String puzzleType, int gridSize, int moves, long timeMs) {
        try {
            long userId = authRepository.getUserIdByUsername(username);
            if (userId <= 0) {
                // Si el usuario no existe aún, intentar registrarlo de forma transparente
                long newId = authRepository.registerUser(username, ("temp" + System.currentTimeMillis()).toCharArray());
                if (newId > 0) {
                    userId = newId;
                } else {
                    return -1;
                }
            }
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues v = new ContentValues();
            v.put(DBHelper.ScoreTable.COL_USER_ID, userId);
            v.put(DBHelper.ScoreTable.COL_PUZZLE_TYPE, puzzleType);
            v.put(DBHelper.ScoreTable.COL_GRID_SIZE, gridSize);
            v.put(DBHelper.ScoreTable.COL_MOVES, moves);
            v.put(DBHelper.ScoreTable.COL_TIME_MS, timeMs);
            v.put(DBHelper.ScoreTable.COL_CREATED_AT, System.currentTimeMillis());
            return db.insert(DBHelper.ScoreTable.TABLE, null, v);
        } catch (Exception e) {
            return -1;
        }
    }

    public List<ScoreRow> getTopScores(String puzzleType, int limit) {
        List<ScoreRow> list = new ArrayList<>();
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            // IMPORTANTE: algunos builds de SQLite en Android no aceptan bind parameter en LIMIT
            String sql = "SELECT s." + DBHelper.ScoreTable.COL_TIME_MS + ", s." + DBHelper.ScoreTable.COL_MOVES + ", u." + DBHelper.UserTable.COL_USERNAME +
                    " FROM " + DBHelper.ScoreTable.TABLE + " s JOIN " + DBHelper.UserTable.TABLE + " u ON s." + DBHelper.ScoreTable.COL_USER_ID + " = u." + DBHelper.UserTable._ID +
                    " WHERE s." + DBHelper.ScoreTable.COL_PUZZLE_TYPE + " = ? ORDER BY s." + DBHelper.ScoreTable.COL_TIME_MS + " ASC LIMIT " + Math.max(1, limit);
            try (Cursor c = db.rawQuery(sql, new String[]{puzzleType})) {
                while (c.moveToNext()) {
                    long timeMs = c.getLong(0);
                    int moves = c.getInt(1);
                    String user = c.getString(2);
                    list.add(new ScoreRow(user, moves, timeMs));
                }
            }
        } catch (Exception e) {
            // Retornar lista vacía en caso de error
        }
        return list;
    }

    public List<ScoreRow> getUserScores(String username, String puzzleType, int limit) {
        List<ScoreRow> list = new ArrayList<>();
        try {
            long userId = authRepository.getUserIdByUsername(username);
            if (userId <= 0) return list;
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String sql = "SELECT s." + DBHelper.ScoreTable.COL_TIME_MS + ", s." + DBHelper.ScoreTable.COL_MOVES +
                    " FROM " + DBHelper.ScoreTable.TABLE + " s " +
                    " WHERE s." + DBHelper.ScoreTable.COL_USER_ID + " = ? " +
                    (puzzleType != null ? (" AND s." + DBHelper.ScoreTable.COL_PUZZLE_TYPE + " = ? ") : "") +
                    " ORDER BY s." + DBHelper.ScoreTable.COL_CREATED_AT + " DESC LIMIT " + Math.max(1, limit);
            String[] args;
            if (puzzleType != null) {
                args = new String[]{String.valueOf(userId), puzzleType};
            } else {
                args = new String[]{String.valueOf(userId)};
            }
            try (Cursor c = db.rawQuery(sql, args)) {
                while (c.moveToNext()) {
                    long timeMs = c.getLong(0);
                    int moves = c.getInt(1);
                    list.add(new ScoreRow(username, moves, timeMs));
                }
            }
        } catch (Exception e) {
            // devolver lista vacía en error
        }
        return list;
    }

    public static class ScoreRow {
        public final String username;
        public final int moves;
        public final long timeMs;

        public ScoreRow(String username, int moves, long timeMs) {
            this.username = username;
            this.moves = moves;
            this.timeMs = timeMs;
        }
    }
}


