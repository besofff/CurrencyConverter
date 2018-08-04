package com.sergeybelkin.currencyconverter.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
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

import com.sergeybelkin.currencyconverter.CurrencyFetcher;
import com.sergeybelkin.currencyconverter.service.Config;
import com.sergeybelkin.currencyconverter.fragment.CurrencyPickerDialogFragment;
import com.sergeybelkin.currencyconverter.database.DatabaseAssistant;
import com.sergeybelkin.currencyconverter.service.ConnectionDetector;
import com.sergeybelkin.currencyconverter.service.Formatter;
import com.sergeybelkin.currencyconverter.R;

import org.json.JSONException;

import java.io.IOException;

import static com.sergeybelkin.currencyconverter.Constants.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CurrencyPickerDialogFragment.NoticeDialogListener {

    private StringBuilder inputString;
    private StringBuilder resultString;
    private DatabaseAssistant assistant;
    private boolean firstRun;

    LinearLayout convertibleCurrency, resultingCurrency;
    ImageView convertibleCurrencyFlag, resultingCurrencyFlag;
    TextView convertibleCurrencyCode, resultingCurrencyCode;

    TextView inputTextView, resultTextView;
    ImageButton b_change;
    ImageButton b_delete;
    Button b_point;

    Handler handler = new Handler();
    ProgressDialog dialog;

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

        loadPrefs();

        assistant = DatabaseAssistant.getInstance(this);

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

        if (firstRun) new FillTableTask().execute();
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
        convertibleCurrency = findViewById(R.id.convertible_currency);
        convertibleCurrency.setOnClickListener(this);
        resultingCurrency = findViewById(R.id.resulting_currency);
        resultingCurrency.setOnClickListener(this);

        convertibleCurrencyFlag = (ImageView) convertibleCurrency.getChildAt(0);
        convertibleCurrencyCode = (TextView) convertibleCurrency.getChildAt(1);
        resultingCurrencyFlag = (ImageView) resultingCurrency.getChildAt(0);
        resultingCurrencyCode = (TextView) resultingCurrency.getChildAt(1);

        Config config = Config.newInstance(getApplicationContext());
        setResources(convertibleCurrencyFlag, config.getConvertibleFlagId(), convertibleCurrencyCode, config.getConvertibleCode());
        setResources(resultingCurrencyFlag, config.getResultingFlagId(), resultingCurrencyCode, config.getResultingCode());
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.convertible_currency:
                CurrencyPickerDialogFragment fragment = new CurrencyPickerDialogFragment();
                fragment.show(getSupportFragmentManager(), DIALOG_TAG_FROM);
                break;
            case R.id.resulting_currency:
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
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_refresh:
                if (!ConnectionDetector.isConnected(this)) {
                    Toast.makeText(this, R.string.msg_update_fail, Toast.LENGTH_SHORT).show();
                } else {
                    new FillTableTask().execute();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadPrefs(){
        firstRun = Config.newInstance(getApplicationContext()).isFirstRun();
    }

    private void convert(){
        if (inputString.length() == 0) return;

        double result = assistant.convert(
                convertibleCurrencyCode.getText().toString(),
                resultingCurrencyCode.getText().toString(),
                inputString.toString());
        cleanResultString();
        resultString.append(Formatter.doubleToString(result));
    }

    private void changeCurrency(){
        Config config = Config.newInstance(this);

        int tempId = config.getConvertibleFlagId();
        String tempCC = config.getConvertibleCode();

        setResources(convertibleCurrencyFlag, config.getResultingFlagId(), convertibleCurrencyCode, config.getResultingCode());
        config.setConvertibleFlagId(config.getResultingFlagId());
        config.setConvertibleCode(config.getResultingCode());

        setResources(resultingCurrencyFlag, tempId, resultingCurrencyCode, tempCC);
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
            setResources(convertibleCurrencyFlag, imageResId, convertibleCurrencyCode, currencyCode);
            config.setConvertibleFlagId(imageResId);
            config.setConvertibleCode(currencyCode);
        } else {
            setResources(resultingCurrencyFlag, imageResId, resultingCurrencyCode, currencyCode);
            config.setResultingFlagId(imageResId);
            config.setResultingCode(currencyCode);
        }

        convert();
        refreshTextViews();
    }

    private class FillTableTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage(getString(R.string.msg_updating));
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                assistant.fillInTable(CurrencyFetcher.getJson(getString(R.string.source)));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            Toast.makeText(getApplicationContext(), getString(R.string.msg_update_success), Toast.LENGTH_SHORT).show();
        }
    }
}