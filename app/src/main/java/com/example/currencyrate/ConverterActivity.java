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
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class ConverterActivity extends AppCompatActivity {

    private EditText etAmountGive;
    private TextView tvAmountReceive, tvExchangeRateText;
    private ImageView btnSwap, btnBack;
    private double currentRate = 92.9343;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);

        etAmountGive = findViewById(R.id.etAmountGive);
        tvAmountReceive = findViewById(R.id.tvAmountReceive);
        tvExchangeRateText = findViewById(R.id.tvExchangeRateText);

        btnSwap = findViewById(R.id.btnSwap);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnSwap.setOnClickListener(v -> {
            RotateAnimation rotate = new RotateAnimation(0, 180, 
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            v.startAnimation(rotate);
            
            // Randomly change the rate when swapping
            currentRate = 1.0 / currentRate;
            if (currentRate < 0.1) currentRate = 60.0 + random.nextDouble() * 40.0;
            
            updateExchangeRateDisplay();
            calculateConversion();
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

        // Set up listeners for the amount buttons (100, 500, etc.)
        setupQuickAmountButtons();
        
        updateExchangeRateDisplay();
        calculateConversion();
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

    private void updateExchangeRateDisplay() {
        tvExchangeRateText.setText(String.format("1 USD = %.4f RUB", currentRate));
    }

    private void calculateConversion() {
        String input = etAmountGive.getText().toString();
        if (input.isEmpty()) {
            tvAmountReceive.setText("0.00");
            return;
        }
        try {
            double amountGive = Double.parseDouble(input);
            double result = amountGive * currentRate;
            tvAmountReceive.setText(String.format("%.2f", result));
            tvAmountReceive.setAlpha(0.5f);
            tvAmountReceive.animate().alpha(1f).setDuration(200).start();
        } catch (NumberFormatException e) {
            tvAmountReceive.setText("0.00");
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
