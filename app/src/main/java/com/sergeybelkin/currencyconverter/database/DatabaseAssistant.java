package com.sergeybelkin.currencyconverter.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.sergeybelkin.currencyconverter.Currency;
import com.sergeybelkin.currencyconverter.service.Config;

import java.util.ArrayList;
import java.util.List;

import static com.sergeybelkin.currencyconverter.Constants.*;

public class DatabaseAssistant {

    private static DatabaseAssistant assistant;
    private SQLiteDatabase database;
    private Context context;
    private Currency convertibleCurrency;
    private Currency resultCurrency;

    private DatabaseAssistant(Context context){
        this.context = context;
        database = new DatabaseHelper(context.getApplicationContext()).getWritableDatabase();
        convertibleCurrency = new Currency();
        resultCurrency = new Currency();
    }

    public static DatabaseAssistant getInstance(Context context){
        if (assistant == null) {
            assistant = new DatabaseAssistant(context);
        }
        return assistant;
    }

    public double convert(String convertibleCC, String resultCC, String value){
        double rateFrom = 0;
        double rateTo = 0;

        if (convertibleCurrency.getCode() != null && convertibleCurrency.getCode().equals(convertibleCC)){
            rateFrom = convertibleCurrency.getRate();
        } else {
            rateFrom = getData(convertibleCC);
            convertibleCurrency.setCode(convertibleCC);
            convertibleCurrency.setRate(rateFrom);
        }

        if (resultCurrency.getCode() != null && resultCurrency.getCode().equals(resultCC)){
            rateTo = resultCurrency.getRate();
        } else {
            rateTo = getData(resultCC);
            resultCurrency.setCode(resultCC);
            resultCurrency.setRate(rateTo);
        }

        return Double.parseDouble(value)*rateFrom/rateTo;
    }

    private double getData(String currencyCode){
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

    public void fillInTable(List<Currency> currencies){

        boolean firstRun = Config.newInstance(context.getApplicationContext()).isFirstRun();

        if (firstRun) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DB_KEY_CC, "UAH");
            contentValues.put(DB_KEY_NAME, "Українська гривня");
            contentValues.put(DB_KEY_RATE, 1.0);
            contentValues.put(DB_KEY_IMAGE_RES_ID, context.getResources().getIdentifier("_uah", "drawable", context.getPackageName()));
            contentValues.put(DB_KEY_FAVORITE, 1);
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
                contentValues.put(DB_KEY_IMAGE_RES_ID, context.getResources().getIdentifier("_"+currencyCode.toLowerCase(), "drawable", context.getPackageName()));
                contentValues.put(DB_KEY_FAVORITE, 1);
                database.insert(TABLE_CURRENCIES, null, contentValues);
            } else {
                database.update(TABLE_CURRENCIES, contentValues, DB_KEY_CC + " = ?", new String[]{currencyCode});
            }
        }

        Cursor cursor = database.query(TABLE_CURRENCIES, null, null, null, null, null, null);
        if (cursor.moveToNext()) {
            Config.newInstance(context).setFirstRun(false);
        } else {
            Config.newInstance(context).setFirstRun(true);
        }
        cursor.close();
    }

    // вызвать с параметрами (DB_KEY_FAVORITE +" = ?", new String[]{"1"}), чтобы получить список избранных валют
    public List<Currency> getCurrenciesList(String where, String[] whereArgs){
        List<Currency> currencies = new ArrayList<>();

        Cursor cursor = database.query(TABLE_CURRENCIES,
                new String[]{DB_KEY_CC, DB_KEY_NAME, DB_KEY_IMAGE_RES_ID, DB_KEY_FAVORITE},
                where, whereArgs, null, null, DB_KEY_FAVORITE +" DESC, "+ DB_KEY_CC);

        while(cursor.moveToNext()){
            Currency currency = new Currency();
            String code = cursor.getString(cursor.getColumnIndex(DB_KEY_CC));
            String name = cursor.getString(cursor.getColumnIndex(DB_KEY_NAME));
            currency.setCode(code);
            currency.setName(name);
            currency.setImageResourceId(cursor.getInt(cursor.getColumnIndex(DB_KEY_IMAGE_RES_ID)));
            currency.setChecked(cursor.getInt(cursor.getColumnIndex(DB_KEY_FAVORITE)) == 1);
            currencies.add(currency);
        }
        cursor.close();

        return currencies;
    }

    private class UpdateFavoritesTask extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String... params) {
            ContentValues values = new ContentValues();
            values.put(DB_KEY_FAVORITE, Integer.parseInt(params[1]));
            database.update(TABLE_CURRENCIES, values, DB_KEY_CC +" = ?", new String[]{params[0]});

            return null;
        }
    }

    public void updateFavorites(String currencyCode, boolean isFavorite){
        String[] params = new String[]{currencyCode, isFavorite ? String.valueOf(1) : String.valueOf(0)};
        new UpdateFavoritesTask().execute(params);
    }
}
