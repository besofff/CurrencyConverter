package com.sergeybelkin.currencyconverter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sergeybelkin.currencyconverter.Api;
import com.sergeybelkin.currencyconverter.Currency;
import com.sergeybelkin.currencyconverter.DatabaseHelper;
import com.sergeybelkin.currencyconverter.service.Config;
import com.sergeybelkin.currencyconverter.CurrencyPickerDialogFragment;
import com.sergeybelkin.currencyconverter.service.ConnectionDetector;
import com.sergeybelkin.currencyconverter.service.Formatter;
import com.sergeybelkin.currencyconverter.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.sergeybelkin.currencyconverter.Constants.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CurrencyPickerDialogFragment.NoticeDialogListener {

    StringBuilder inputString;
    StringBuilder resultString;
    DatabaseHelper helper;
    boolean firstRun;

    LinearLayout sourceCurrency, resultCurrency;
    ImageView sourceCurrencyFlag, resultCurrencyFlag;
    TextView sourceCurrencyCode, resultCurrencyCode;

    TextView inputTextView, resultTextView;
    ImageButton b_change;
    ImageButton b_delete;
    Button b_point;
    Handler handler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (inputString == null || inputString.length() == 0) return;

            inputString.deleteCharAt(inputString.length() - 1);
            if (inputString.length() == 0){
                cleanResultString();
            } else {
                convert();
            }
            refreshTextViews();

            if (b_delete.isPressed())
                handler.postDelayed(runnable, 200);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = DatabaseHelper.getInstance(this);

        loadPrefs();
        if (firstRun) loadUpdates();

        inputTextView = findViewById(R.id.input_text_view);
        resultTextView = findViewById(R.id.result_text_view);
        initializeButtons();
        initializeLayouts();

        if (savedInstanceState != null) {
            inputString = (StringBuilder) savedInstanceState.getSerializable(BUNDLE_KEY_INPUT_STRING);
            resultString = (StringBuilder) savedInstanceState.getSerializable(BUNDLE_KEY_RESULT_STRING);
            refreshTextViews();
        } else {
            inputString = new StringBuilder();
            resultString = new StringBuilder();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BUNDLE_KEY_INPUT_STRING, inputString);
        outState.putSerializable(BUNDLE_KEY_RESULT_STRING, resultString);
    }

    private void initializeButtons(){
        View grid = findViewById(R.id.numeric_keyboard);
        for (int i = 0; i<10; i++) {
            View view = grid.findViewWithTag(String.valueOf(i));
            view.setOnClickListener(this);
        }

        b_change = findViewById(R.id.button_change);
        b_change.setOnClickListener(this);

        b_point = findViewById(R.id.button_point);
        b_point.setOnClickListener(this);

        b_delete = findViewById(R.id.button_delete);
        b_delete.setOnClickListener(this);
        b_delete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                handler.postDelayed(runnable, 200);
                return true;
            }
        });
    }

    private void initializeLayouts(){
        sourceCurrency = findViewById(R.id.source_currency);
        sourceCurrency.setOnClickListener(this);
        resultCurrency = findViewById(R.id.result_currency);
        resultCurrency.setOnClickListener(this);

        sourceCurrencyFlag = (ImageView) sourceCurrency.getChildAt(0);
        sourceCurrencyCode = (TextView) sourceCurrency.getChildAt(1);
        resultCurrencyFlag = (ImageView) resultCurrency.getChildAt(0);
        resultCurrencyCode = (TextView) resultCurrency.getChildAt(1);

        Config config = Config.newInstance(getApplicationContext());
        setResources(sourceCurrencyFlag, config.getConvertibleFlagId(), sourceCurrencyCode, config.getConvertibleCode());
        setResources(resultCurrencyFlag, config.getResultingFlagId(), resultCurrencyCode, config.getResultingCode());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.source_currency:
                CurrencyPickerDialogFragment fragment = new CurrencyPickerDialogFragment();
                fragment.show(getSupportFragmentManager(), DIALOG_TAG_FROM);
                break;
            case R.id.result_currency:
                fragment = new CurrencyPickerDialogFragment();
                fragment.show(getSupportFragmentManager(), DIALOG_TAG_TO);
                break;
            case R.id.button_change:
                changeCurrency();
                break;
            case R.id.button_point:
                if (!inputString.toString().contains(".")) {
                    if (inputString.length() == 0) {
                        inputString.append(0);
                    } inputString.append(".");
                }
                break;
            case R.id.button_delete:
                if (inputString.length() > 0) inputString.deleteCharAt(inputString.length()-1);
                break;
            default:
                int number = Integer.parseInt(view.getTag().toString());
                if (inputString.toString().equals("0")){
                    inputString.deleteCharAt(inputString.length()-1);
                } inputString.append(number);
                break;
        }

        convert();
        if (inputString.length() == 0) {
            cleanResultString();
        }
        refreshTextViews();
    }

    private void refreshTextViews(){
        inputTextView.setText(inputString.toString());
        inputTextView.post(new Runnable() {
            @Override
            public void run() {
                ((HorizontalScrollView) findViewById(R.id.scroll_input_view))
                        .fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        });

        resultTextView.setText(resultString.toString());
        resultTextView.post(new Runnable() {
            @Override
            public void run() {
                ((HorizontalScrollView) findViewById(R.id.scroll_result_view))
                        .fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        });
    }

    private void cleanResultString(){
        if (resultString.length() > 0) {
            resultString.delete(0, resultString.length());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_refresh:
                loadUpdates();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadPrefs(){
        firstRun = Config.newInstance(getApplicationContext()).isFirstRun();
    }

    private void convert(){
        if (inputString.length() == 0) return;

        double result = helper.getConvertedRate(
                sourceCurrencyCode.getText().toString(),
                resultCurrencyCode.getText().toString(),
                inputString.toString());
        cleanResultString();
        resultString.append(Formatter.doubleToString(result));
    }

    private void changeCurrency(){
        Config config = Config.newInstance(this);

        int tempId = config.getConvertibleFlagId();
        String tempCC = config.getConvertibleCode();

        setResources(sourceCurrencyFlag, config.getResultingFlagId(), sourceCurrencyCode, config.getResultingCode());
        config.setConvertibleFlagId(config.getResultingFlagId());
        config.setConvertibleCode(config.getResultingCode());

        setResources(resultCurrencyFlag, tempId, resultCurrencyCode, tempCC);
        config.setResultingFlagId(tempId);
        config.setResultingCode(tempCC);
    }

    private void setResources(ImageView imageView, int imageResId, TextView textView, String text){
        imageView.setImageResource(imageResId);
        textView.setText(text);
    }

    @Override
    public void onDialogItemClick(DialogFragment dialog, int imageResId, String currencyCode) {
        Config config = Config.newInstance(getApplicationContext());

        if (dialog.getTag().equals(DIALOG_TAG_FROM)){
            setResources(sourceCurrencyFlag, imageResId, sourceCurrencyCode, currencyCode);
            config.setConvertibleFlagId(imageResId);
            config.setConvertibleCode(currencyCode);
        } else {
            setResources(resultCurrencyFlag, imageResId, resultCurrencyCode, currencyCode);
            config.setResultingFlagId(imageResId);
            config.setResultingCode(currencyCode);
        }

        convert();
        refreshTextViews();
    }

    private void loadUpdates() {

        if (ConnectionDetector.isConnected(this)){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.base_url))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            Api api = retrofit.create(Api.class);
            Call<List<Currency>> call = api.getAllCurrencies();
            call.enqueue(new Callback<List<Currency>>() {
                @Override
                public void onResponse(Call<List<Currency>> call, Response<List<Currency>> response) {
                    helper.updateDB(MainActivity.this, response.body());
                }

                @Override
                public void onFailure(Call<List<Currency>> call, Throwable t) {

                }
            });
        } else {
            Toast.makeText(this, R.string.msg_update_fail, Toast.LENGTH_SHORT).show();
        }
    }
}