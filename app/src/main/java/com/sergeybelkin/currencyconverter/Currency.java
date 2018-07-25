package com.sergeybelkin.currencyconverter;

public class Currency {

    private String code;
    private String name;
    private double rate;
    private boolean isChecked;
    private int imageResourceId;

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
