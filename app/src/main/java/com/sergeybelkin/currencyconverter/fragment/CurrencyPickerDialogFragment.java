package com.sergeybelkin.currencyconverter.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sergeybelkin.currencyconverter.Currency;
import com.sergeybelkin.currencyconverter.database.DatabaseAssistant;
import com.sergeybelkin.currencyconverter.R;

import java.util.List;

import static com.sergeybelkin.currencyconverter.Constants.*;

public class CurrencyPickerDialogFragment extends DialogFragment {

    private List<Currency> currencies;

    public interface NoticeDialogListener {
        void onDialogItemClick(DialogFragment dialog, int imageResId, String currencyCode);
    }

    NoticeDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mListener = (NoticeDialogListener) activity;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        RecyclerView currencyRecyclerView = new RecyclerView(getContext());
        currencyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DatabaseAssistant assistant = DatabaseAssistant.getInstance(getContext());
        currencies = assistant.getCurrenciesList(DB_KEY_FAVORITE +" = ?", new String[]{"1"});
        currencyRecyclerView.setAdapter(new CurrencyAdapter());

        return new AlertDialog.Builder(getActivity())
                .setView(currencyRecyclerView)
                .create();
    }

    private class CurrencyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView currencyFlag;
        private TextView currencyCode;
        private TextView currencyName;
        private int imageResourceId;

        CurrencyHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            currencyFlag = itemView.findViewById(R.id.dialog_list_item_currency_flag);
            currencyCode = itemView.findViewById(R.id.dialog_list_item_currency_code);
            currencyName = itemView.findViewById(R.id.dialog_list_item_currency_name);
        }

        void bindCurrency(Currency currency) {
            imageResourceId = currency.getImageResourceId();
            currencyFlag.setImageResource(imageResourceId);
            currencyCode.setText(currency.getCode());
            currencyName.setText(currency.getName());
        }

        @Override
        public void onClick(View view) {
            mListener.onDialogItemClick(CurrencyPickerDialogFragment.this, imageResourceId, currencyCode.getText().toString());
            dismiss();
        }
    }

    private class CurrencyAdapter extends RecyclerView.Adapter<CurrencyHolder> {

        @Override
        public CurrencyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View view = layoutInflater.inflate(R.layout.dialog_list_item_currency, parent, false);
            return new CurrencyHolder(view);
        }

        @Override
        public void onBindViewHolder(final CurrencyHolder holder, int position) {
            final Currency currency = currencies.get(position);
            holder.bindCurrency(currency);
        }

        @Override
        public int getItemCount() {
            return currencies.size();
        }
    }
}
