package com.example.chatapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    static final String TABLE_NAME = "server";
    static final String COLUMN_MESSAGES = "messages";
    static final String DB_NAME = "chat_database.DB";
    static final int DB_VERSION = 1;

    //queries
    public String createTable(String TABLE_NAME)
    {
        String s = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + COLUMN_MESSAGES + " TEXT NOT NULL);";
        return s;
    }

    public void insert(String who, String what, String message)
    {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(createTable(who));
        ContentValues contentValue = new ContentValues();
        String msg = what + "%@" + message;
        contentValue.put(DatabaseHelper.COLUMN_MESSAGES, msg);
        database.insert(who, null, contentValue);
        database.close();
    }

    public Cursor fetch(String tableName)
    {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(createTable(tableName));
        database.close();
        String QUERY = "SELECT * FROM " + tableName;
        Cursor cursor = getReadableDatabase().rawQuery(QUERY, null);
        if(cursor != null)
            cursor.moveToFirst();
        return cursor;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(createTable(TABLE_NAME));
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
