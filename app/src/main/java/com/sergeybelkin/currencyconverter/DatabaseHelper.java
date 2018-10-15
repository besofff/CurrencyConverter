package com.sergeybelkin.currencyconverter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sergeybelkin.currencyconverter.service.Config;

import java.util.ArrayList;
import java.util.List;

import static com.sergeybelkin.currencyconverter.Constants.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "list_of_currencies";
    private static final int DB_VERSION = 1;
    private static DatabaseHelper singleton;

    public static DatabaseHelper getInstance(Context context){
        if (singleton == null) {
            singleton = new DatabaseHelper(context);
        }
        return singleton;
    }

    private DatabaseHelper(Context context){
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
                DB_KEY_IS_FAVORITE +" INTEGER, "+
                DB_KEY_IMAGE_RES_ID +" INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}

    public void updateDB(final Context context, final List<Currency> currencies){
        new Thread(new Runnable() {
            @Override
            public void run() {
                update(context, currencies);
            }
        }).start();
    }

    private void update(Context context, List<Currency> currencies) {

        SQLiteDatabase database = getWritableDatabase();
        boolean firstRun = Config.newInstance(context.getApplicationContext()).isFirstRun();

        database.beginTransaction();
        if (firstRun) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DB_KEY_CC, "UAH");
            contentValues.put(DB_KEY_NAME, "Українська гривня");
            contentValues.put(DB_KEY_RATE, 1.0);
            contentValues.put(DB_KEY_IMAGE_RES_ID, context.getResources().getIdentifier("_uah", "drawable", context.getPackageName()));
            contentValues.put(DB_KEY_IS_FAVORITE, 1);
            database.insert(TABLE_CURRENCIES, null, contentValues);
        }

        for (int i = 0; i < currencies.size(); i++) {
            Currency currency = currencies.get(i);
            String currencyCode = currency.getCode();

            if (currencyCode.equals("   ") || currencyCode.equals("UAH")) continue;

            String name = currency.getName();
            double rate = currency.getRate();
            String date = currency.getDate();

            ContentValues contentValues = new ContentValues();
            contentValues.put(DB_KEY_CC, currencyCode);
            contentValues.put(DB_KEY_RATE, rate);
            contentValues.put(DB_KEY_DATE, date);

            if (firstRun) {
                contentValues.put(DB_KEY_NAME, name);
                contentValues.put(DB_KEY_IMAGE_RES_ID, context.getResources().getIdentifier("_" + currencyCode.toLowerCase(), "drawable", context.getPackageName()));
                contentValues.put(DB_KEY_IS_FAVORITE, 1);
                database.insert(TABLE_CURRENCIES, null, contentValues);
            } else {
                database.update(TABLE_CURRENCIES, contentValues, DB_KEY_CC + " = ?", new String[]{currencyCode});
            }
        }

        database.setTransactionSuccessful();
        database.endTransaction();

        Cursor cursor = database.query(TABLE_CURRENCIES, null, null, null, null, null, null);
        if (cursor.moveToNext()) {
            Config.newInstance(context).setFirstRun(false);
        } else {
            Config.newInstance(context).setFirstRun(true);
        }
        cursor.close();
    }

    private double getRate(String currencyCode){

        SQLiteDatabase database = getReadableDatabase();
        double rate = 0;
        Cursor cursor = database.query(TABLE_CURRENCIES,
                new String[]{DB_KEY_CC, DB_KEY_RATE},
                DB_KEY_CC +" = ?",
                new String[]{currencyCode},
                null, null, null);
        if (cursor.moveToNext()){
            rate = cursor.getDouble(cursor.getColumnIndex(DB_KEY_RATE));
        }
        cursor.close();
        return rate;
    }

    public double getConvertedRate(String sourceCC, String resultCC, String value){
        double rateFrom = getRate(sourceCC);
        double rateTo = getRate(resultCC);

        return Double.parseDouble(value)*rateFrom/rateTo;
    }

    public List<Currency> getCurrenciesList(String where, String[] whereArgs){

        SQLiteDatabase database = getReadableDatabase();
        List<Currency> currencies = new ArrayList<>();

        Cursor cursor = database.query(TABLE_CURRENCIES,
                new String[]{DB_KEY_CC, DB_KEY_NAME, DB_KEY_IMAGE_RES_ID, DB_KEY_IS_FAVORITE},
                where, whereArgs, null, null, DB_KEY_IS_FAVORITE +" DESC, "+ DB_KEY_CC);

        while(cursor.moveToNext()){
            Currency currency = new Currency();
            String code = cursor.getString(cursor.getColumnIndex(DB_KEY_CC));
            String name = cursor.getString(cursor.getColumnIndex(DB_KEY_NAME));
            currency.setCode(code);
            currency.setName(name);
            currency.setImageResourceId(cursor.getInt(cursor.getColumnIndex(DB_KEY_IMAGE_RES_ID)));
            currency.setChecked(cursor.getInt(cursor.getColumnIndex(DB_KEY_IS_FAVORITE)) == 1);
            currencies.add(currency);
        }
        cursor.close();

        return currencies;
    }

    public void updateFavorites(final String currencyCode, final boolean isFavorite){

        final SQLiteDatabase database = getWritableDatabase();

        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentValues values = new ContentValues();
                values.put(DB_KEY_IS_FAVORITE, isFavorite ? 1 : 0);
                database.update(TABLE_CURRENCIES, values, DB_KEY_CC +" = ?", new String[]{currencyCode});
            }
        }).start();
    }
}
