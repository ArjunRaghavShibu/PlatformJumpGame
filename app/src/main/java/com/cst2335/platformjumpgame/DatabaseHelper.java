package com.cst2335.platformjumpgame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "scoresDB";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE scores (id INTEGER PRIMARY KEY, score INTEGER)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS scores");
        onCreate(db);
    }

    public void saveScore(int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("score", score);
        db.insert("scores", null, values);
        db.close();
    }

    public int getTopScore() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM scores ORDER BY score DESC LIMIT 1", null);

        if (cursor != null && cursor.moveToFirst()) {
            int scoreColumnIndex = cursor.getColumnIndex("score");

            if (scoreColumnIndex != -1) {
                int topScore = cursor.getInt(scoreColumnIndex);
                cursor.close();
                return topScore;
            }
        }

        return 0;
    }
}

