package com.sergeybelkin.currencyconverter.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sergeybelkin.currencyconverter.Currency;
import com.sergeybelkin.currencyconverter.database.DatabaseAssistant;
import com.sergeybelkin.currencyconverter.R;

import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView currencyRecyclerView;
    private DatabaseAssistant assistant;
    private List<Currency> currencies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        currencyRecyclerView = findViewById(R.id.currency_recycler_view);
        currencyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        assistant = DatabaseAssistant.getInstance(getApplicationContext());
        currencies = assistant.getCurrenciesList(null, null);
        currencyRecyclerView.setAdapter(new CurrencyAdapter());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favorites, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_select_all:
                for (int i = 0; i < currencies.size(); i++){
                    currencies.get(i).setChecked(true);
                    assistant.refreshFavorites(currencies.get(i).getCode(), true);
                }
                break;
            case R.id.action_deselect_all:
                for (int i = 0; i < currencies.size(); i++){
                    currencies.get(i).setChecked(false);
                    assistant.refreshFavorites(currencies.get(i).getCode(), false);
                }
                break;
        }
        currencyRecyclerView.getAdapter().notifyDataSetChanged();

        return super.onOptionsItemSelected(item);
    }

    private class CurrencyHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {

        private Currency currency;
        private ImageView currencyFlag;
        private TextView currencyCode;
        private TextView currencyName;
        private CheckBox isChecked;

        CurrencyHolder(View itemView) {
            super(itemView);
            currencyFlag = itemView.findViewById(R.id.favorite_list_item_currency_flag);
            currencyCode = itemView.findViewById(R.id.favorite_list_item_currency_code);
            currencyName = itemView.findViewById(R.id.favorite_list_item_currency_name);
            isChecked = itemView.findViewById(R.id.favorite_list_item_currency_checkbox);
            isChecked.setOnCheckedChangeListener(this);
        }

        void bindCurrency(Currency currency) {
            this.currency = currency;
            currencyFlag.setImageResource(currency.getImageResourceId());
            currencyCode.setText(currency.getCode());
            currencyName.setText(currency.getName());
            isChecked.setChecked(currency.isChecked());
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            assistant.refreshFavorites(currency.getCode(), isChecked);
        }
    }

    private class CurrencyAdapter extends RecyclerView.Adapter<CurrencyHolder> {

        @Override
        public CurrencyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
            View view = layoutInflater.inflate(R.layout.favorite_list_item_currency, parent, false);
            return new CurrencyHolder(view);
        }

        @Override
        public void onBindViewHolder(final CurrencyHolder holder, int position) {
            Currency currency = currencies.get(position);
            holder.bindCurrency(currency);
        }

        @Override
        public int getItemCount() {
            return currencies.size();
        }
    }
}
