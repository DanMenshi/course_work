package com.example.currencyrate;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ConverterActivity extends AppCompatActivity {

    private EditText etAmountGive;
    private TextView tvAmountReceive, tvExchangeRateText;
    private TextView tvCodeGive, tvNameGive, tvCodeReceive, tvNameReceive;
    private LinearLayout spinnerGive, spinnerReceive;
    private ImageView btnSwap, btnBack;
    
    private Currency currencyGive;
    private Currency currencyReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);

        // Initialize currencies
        currencyGive = CurrencyData.getByCode("USD");
        currencyReceive = CurrencyData.getByCode("RUB");

        // Bind views
        etAmountGive = findViewById(R.id.etAmountGive);
        tvAmountReceive = findViewById(R.id.tvAmountReceive);
        tvExchangeRateText = findViewById(R.id.tvExchangeRateText);
        
        tvCodeGive = ((ViewGroup)findViewById(R.id.spinnerGive)).getChildAt(0) instanceof TextView ? (TextView)((ViewGroup)findViewById(R.id.spinnerGive)).getChildAt(0) : null;
        tvCodeReceive = ((ViewGroup)findViewById(R.id.spinnerReceive)).getChildAt(0) instanceof TextView ? (TextView)((ViewGroup)findViewById(R.id.spinnerReceive)).getChildAt(0) : null;
        
        // Better way to find those text views
        findInternalViews();

        btnSwap = findViewById(R.id.btnSwap);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnSwap.setOnClickListener(v -> {
            swapCurrencies();
            RotateAnimation rotate = new RotateAnimation(0, 180, 
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            v.startAnimation(rotate);
        });

        etAmountGive.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateConversion();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupQuickAmountButtons();
        updateUI();
        
        // Fetch fresh rates from CBR
        CurrencyData.fetchRates(this::updateUI);
    }

    private void findInternalViews() {
        // Find labels inside the CardViews
        ViewGroup cvGiveLayout = (ViewGroup) ((ViewGroup) findViewById(R.id.cvGive)).getChildAt(0);
        for(int i=0; i<cvGiveLayout.getChildCount(); i++) {
            View child = cvGiveLayout.getChildAt(i);
            if(child instanceof TextView && !(child instanceof EditText) && i == cvGiveLayout.getChildCount()-1) {
                tvNameGive = (TextView) child;
            }
        }

        ViewGroup cvReceiveLayout = (ViewGroup) ((ViewGroup) findViewById(R.id.cvReceive)).getChildAt(0);
        for(int i=0; i<cvReceiveLayout.getChildCount(); i++) {
            View child = cvReceiveLayout.getChildAt(i);
            if(child instanceof TextView && i == cvReceiveLayout.getChildCount()-1) {
                tvNameReceive = (TextView) child;
            }
        }
        
        spinnerGive = findViewById(R.id.spinnerGive);
        spinnerReceive = findViewById(R.id.spinnerReceive);
        
        tvCodeGive = (TextView) spinnerGive.getChildAt(0);
        tvCodeReceive = (TextView) spinnerReceive.getChildAt(0);
    }

    private void swapCurrencies() {
        Currency temp = currencyGive;
        currencyGive = currencyReceive;
        currencyReceive = temp;
        updateUI();
    }

    private void updateUI() {
        if (tvCodeGive != null) tvCodeGive.setText(currencyGive.getSymbol() + " " + currencyGive.getCode());
        if (tvNameGive != null) tvNameGive.setText(currencyGive.getName());
        if (tvCodeReceive != null) tvCodeReceive.setText(currencyReceive.getSymbol() + " " + currencyReceive.getCode());
        if (tvNameReceive != null) tvNameReceive.setText(currencyReceive.getName());

        double rate = currencyGive.getRate() / currencyReceive.getRate();
        tvExchangeRateText.setText(String.format("1 %s = %.4f %s", currencyGive.getCode(), rate, currencyReceive.getCode()));
        
        calculateConversion();
    }

    private void calculateConversion() {
        String input = etAmountGive.getText().toString();
        if (input.isEmpty()) {
            tvAmountReceive.setText("0.00");
            return;
        }
        try {
            double amountGive = Double.parseDouble(input);
            double rate = currencyGive.getRate() / currencyReceive.getRate();
            double result = amountGive * rate;
            tvAmountReceive.setText(String.format("%.2f", result));
        } catch (NumberFormatException e) {
            tvAmountReceive.setText("0.00");
        }
    }

    private void setupQuickAmountButtons() {
        View root = findViewById(android.R.id.content);
        findAllButtons(root);
    }

    private void findAllButtons(View v) {
        if (v instanceof Button) {
            v.setOnClickListener(view -> {
                animateClick(view);
                String amount = ((Button) view).getText().toString();
                etAmountGive.setText(amount);
            });
        } else if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                findAllButtons(vg.getChildAt(i));
            }
        }
    }

    private void animateClick(View v) {
        ScaleAnimation anim = new ScaleAnimation(1f, 0.95f, 1f, 0.95f, 
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(100);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(1);
        v.startAnimation(anim);
    }
}
