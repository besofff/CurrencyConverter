package com.sergeybelkin.currencyconverter.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.sergeybelkin.currencyconverter.Constants;

public class Config {

    private SharedPreferences preferences;
    private Context context;

    public static Config newInstance(Context context) {
        return new Config(context);
    }

    public Config(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(Constants.APP_PREFS, Context.MODE_PRIVATE);
    }

    public boolean isFirstRun() {
        return preferences.getBoolean(Constants.PREFS_KEY_FIRST_RUN, true);
    }

    public void setFirstRun(boolean firstRun) {
        preferences.edit().putBoolean(Constants.PREFS_KEY_FIRST_RUN, firstRun).apply();
    }

    public int getConvertibleFlagId() {
        return preferences.getInt(Constants.PREFS_KEY_CONVERTIBLE_CURRENCY_FLAG, context.getResources().getIdentifier("_uah", "drawable", context.getPackageName()));
    }

    public void setConvertibleFlagId(int resourceId) {
        preferences.edit().putInt(Constants.PREFS_KEY_CONVERTIBLE_CURRENCY_FLAG, resourceId).apply();
    }

    public int getResultingFlagId() {
        return preferences.getInt(Constants.PREFS_KEY_RESULTING_CURRENCY_FLAG, context.getResources().getIdentifier("_uah", "drawable", context.getPackageName()));
    }

    public void setResultingFlagId(int resourceId) {
        preferences.edit().putInt(Constants.PREFS_KEY_RESULTING_CURRENCY_FLAG, resourceId).apply();
    }

    public String getConvertibleCode() {
        return preferences.getString(Constants.PREFS_KEY_CONVERTIBLE_CURRENCY_CODE, "UAH");
    }

    public void setConvertibleCode(String code) {
        preferences.edit().putString(Constants.PREFS_KEY_CONVERTIBLE_CURRENCY_CODE, code).apply();
    }

    public String getResultingCode() {
        return preferences.getString(Constants.PREFS_KEY_RESULTING_CURRENCY_CODE, "UAH");
    }

    public void setResultingCode(String code) {
        preferences.edit().putString(Constants.PREFS_KEY_RESULTING_CURRENCY_CODE, code).apply();
    }
}
