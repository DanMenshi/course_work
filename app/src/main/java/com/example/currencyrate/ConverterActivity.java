package com.example.currencyrate;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ConverterActivity extends AppCompatActivity {

    private EditText etAmountGive;
    private TextView tvAmountReceive;
    private ImageView btnSwap;
    private ImageView btnBack;

    private final double CURRENT_USD_RATE = 92.9343;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);

        etAmountGive = findViewById(R.id.etAmountGive);
        tvAmountReceive = findViewById(R.id.tvAmountReceive);
        btnSwap = findViewById(R.id.btnSwap);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnSwap.setOnClickListener(v -> {
            // Анимация вращения при нажатии на кнопку смены
            RotateAnimation rotate = new RotateAnimation(0, 180, 
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            v.startAnimation(rotate);
            
            // Логика смены валют (в данном демо просто визуальный эффект)
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

        calculateConversion();
    }

    private void calculateConversion() {
        String input = etAmountGive.getText().toString();
        if (input.isEmpty()) {
            tvAmountReceive.setText("0");
            return;
        }

        try {
            double amountGive = Double.parseDouble(input);
            double result = amountGive * CURRENT_USD_RATE;
            tvAmountReceive.setText(String.format("%.2f", result));
            
            // Маленькая анимация появления текста при изменении
            tvAmountReceive.setAlpha(0.5f);
            tvAmountReceive.animate().alpha(1f).setDuration(200).start();
            
        } catch (NumberFormatException e) {
            tvAmountReceive.setText("0.00");
        }
    }
}