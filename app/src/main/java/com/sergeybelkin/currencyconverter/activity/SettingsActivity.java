package com.sergeybelkin.currencyconverter.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sergeybelkin.currencyconverter.R;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    TextView settingsFavorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsFavorites = (TextView) findViewById(R.id.settings_favorites);
        settingsFavorites.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent;
        switch (id){
            case R.id.settings_favorites:
                intent = new Intent(this, FavoritesActivity.class);
                startActivity(intent);
                break;

        }
    }
}
