package com.example.a4laba_plotnikova;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Информация о базе данных
    public static final String DATABASE_NAME = "tracks.db";
    public static final int DATABASE_VERSION = 1;

    // Информация о таблице
    public static final String TABLE_NAME = "track_stats";
    public static final String COLUMN_ID = "_id";  // Переименовали в _id для совместимости
    public static final String COLUMN_ARTIST = "artist";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // Конструктор
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Создание таблицы с колонкой _id
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // Используем _id для совместимости
                COLUMN_ARTIST + " TEXT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_TIMESTAMP + " TEXT)";
        db.execSQL(createTable);
    }

    // Обновление базы данных
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Метод для вставки нового трека в таблицу
    public void insertTrack(String artist, String title, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ARTIST, artist);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_TIMESTAMP, timestamp);
        db.insert(TABLE_NAME, null, values);
    }

    // Метод для получения всех треков из таблицы с колонкой _id для SimpleCursorAdapter
    public Cursor getAllTracks() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Запрос, который возвращает все колонки, переименовывая COLUMN_ID в _id
        return db.query(TABLE_NAME,
                new String[]{COLUMN_ID, COLUMN_ARTIST, COLUMN_TITLE, COLUMN_TIMESTAMP},
                null, null, null, null,
                COLUMN_TIMESTAMP + " DESC");
    }
}