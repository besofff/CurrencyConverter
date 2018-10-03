package com.sergeybelkin.currencyconverter.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.sergeybelkin.currencyconverter.Currency;
import com.sergeybelkin.currencyconverter.database.DatabaseAssistant;
import com.sergeybelkin.currencyconverter.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoritesActivity extends AppCompatActivity {

    private DatabaseAssistant assistant;
    private List<Currency> currencies;
    private SimpleAdapter adapter;
    private List<Map<String, Object>> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        assistant = DatabaseAssistant.getInstance(getApplicationContext());
        currencies = assistant.getCurrenciesList(null, null);

        list = new ArrayList<>();

        for (int i = 0; i < currencies.size(); i++){
            Map<String, Object> m = new HashMap<>();
            m.put("flag", currencies.get(i).getImageResourceId());
            m.put("code", currencies.get(i).getCode());
            m.put("name", currencies.get(i).getName());
            m.put("isChecked", currencies.get(i).isChecked());
            list.add(m);
        }

        String[] from = new String[]{"flag", "code", "name", "isChecked"};
        int[] to = new int[]{R.id.fav_list_item_flag,
                R.id.fav_list_item_code,
                R.id.fav_list_item_name,
                R.id.fav_list_item_checkbox};

        adapter = new SimpleAdapter(this, list, R.layout.favorite_list_item, from, to);
        ListView listView = findViewById(R.id.favorites_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                CheckBox checkBox = view.findViewById(R.id.fav_list_item_checkbox);
                boolean isChecked = (Boolean)list.get(position).get("isChecked");
                checkBox.setChecked(!isChecked);
                updateFavorites(position, !isChecked);
                adapter.notifyDataSetChanged();

            }
        });
        listView.setAdapter(adapter);
    }

    private void updateFavorites(int position, boolean isChecked){
        list.get(position).put("isChecked", isChecked);
        assistant.updateFavorites(currencies.get(position).getCode(), isChecked);
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
                for (int i = 0; i < list.size(); i++){
                    updateFavorites(i, true);
                }
                break;
            case R.id.action_deselect_all:
                for (int i = 0; i < list.size(); i++){
                    updateFavorites(i, false);
                }
                break;
        }

        adapter.notifyDataSetChanged();

        return super.onOptionsItemSelected(item);
    }
}
