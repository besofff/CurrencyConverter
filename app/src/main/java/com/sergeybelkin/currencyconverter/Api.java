package com.sergeybelkin.currencyconverter;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface Api {

    @GET("exchange?json")
    Call<List<Currency>> getAllCurrencies();
}
