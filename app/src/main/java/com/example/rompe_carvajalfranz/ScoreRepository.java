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
        long userId = authRepository.getUserIdByUsername(username);
        if (userId <= 0) return -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DBHelper.ScoreTable.COL_USER_ID, userId);
        v.put(DBHelper.ScoreTable.COL_PUZZLE_TYPE, puzzleType);
        v.put(DBHelper.ScoreTable.COL_GRID_SIZE, gridSize);
        v.put(DBHelper.ScoreTable.COL_MOVES, moves);
        v.put(DBHelper.ScoreTable.COL_TIME_MS, timeMs);
        v.put(DBHelper.ScoreTable.COL_CREATED_AT, System.currentTimeMillis());
        return db.insert(DBHelper.ScoreTable.TABLE, null, v);
    }

    public List<ScoreRow> getTopScores(String puzzleType, int limit) {
        List<ScoreRow> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT s." + DBHelper.ScoreTable.COL_TIME_MS + ", s." + DBHelper.ScoreTable.COL_MOVES + ", u." + DBHelper.UserTable.COL_USERNAME +
                " FROM " + DBHelper.ScoreTable.TABLE + " s JOIN " + DBHelper.UserTable.TABLE + " u ON s." + DBHelper.ScoreTable.COL_USER_ID + " = u." + DBHelper.UserTable._ID +
                " WHERE s." + DBHelper.ScoreTable.COL_PUZZLE_TYPE + " = ? ORDER BY s." + DBHelper.ScoreTable.COL_TIME_MS + " ASC LIMIT ?";
        try (Cursor c = db.rawQuery(sql, new String[]{puzzleType, String.valueOf(limit)})) {
            while (c.moveToNext()) {
                long timeMs = c.getLong(0);
                int moves = c.getInt(1);
                String user = c.getString(2);
                list.add(new ScoreRow(user, moves, timeMs));
            }
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


