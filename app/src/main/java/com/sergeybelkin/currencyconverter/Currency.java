package com.sergeybelkin.currencyconverter;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Currency {

    @SerializedName("cc")
    @Expose
    private String code;

    @SerializedName("txt")
    @Expose
    private String name;

    @SerializedName("rate")
    @Expose
    private double rate;

    @SerializedName("exchangedate")
    @Expose
    private String date;

    private boolean isChecked;
    private int imageResourceId;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public double getRate() {
        return rate;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRate (double rate) {
        this.rate = rate;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }
}
