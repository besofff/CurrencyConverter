package com.sergeybelkin.currencyconverter.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.sergeybelkin.currencyconverter.Constants.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Converter";
    private static final int DB_VERSION = 1;

    DatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE "+ TABLE_CURRENCIES +" ("+
                DB_KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                DB_KEY_CC +" TEXT, "+
                DB_KEY_NAME +" TEXT, "+
                DB_KEY_RATE +" REAL, "+
                DB_KEY_DATE +" TEXT, "+
                DB_KEY_FAVORITE +" INTEGER, "+
                DB_KEY_IMAGE_RES_ID +" INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
