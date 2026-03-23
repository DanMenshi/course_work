package com.example.currencyrate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardView btnOpenConverter = findViewById(R.id.btnOpenConverter);
        btnOpenConverter.setOnClickListener(v -> {
            animateClick(v);
            startActivity(new Intent(MainActivity.this, ConverterActivity.class));
        });

        findViewById(R.id.ivRefresh).setOnClickListener(v -> {
            v.animate().rotationBy(360).setDuration(500).start();
            CurrencyData.fetchRates(this::updateGridData);
        });

        // Initial fetch
        CurrencyData.fetchRates(this::updateGridData);
        updateGridData();
    }

    private void updateGridData() {
        ViewGroup gridLayout = findViewById(R.id.glCurrencies);
        List<Currency> currencies = CurrencyData.getCurrencies();

        // Map static views to data (for simplicity in this layout)
        updateCard(gridLayout.getChildAt(0), "USD");
        updateCard(gridLayout.getChildAt(1), "EUR");
        updateCard(gridLayout.getChildAt(2), "CNY");
        updateCard(gridLayout.getChildAt(3), "KZT");
    }

    private void updateCard(View card, String code) {
        if (card == null) return;
        Currency c = CurrencyData.getByCode(code);
        
        card.setOnClickListener(v -> {
            animateClick(v);
            Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
            intent.putExtra("CURRENCY_CODE", c.getCode());
            intent.putExtra("CURRENCY_RATE", String.format("%.2f", c.getRate()));
            startActivity(intent);
        });

        if (card instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) card;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                if (child instanceof ViewGroup) {
                    ViewGroup labels = (ViewGroup) child;
                    TextView tvPair = (TextView) labels.getChildAt(0);
                    TextView tvValue = (TextView) labels.getChildAt(1);
                    
                    tvPair.setText("1 " + c.getCode() + " / RUB");
                    tvValue.setText(String.format("%.2f", c.getRate()));
                }
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
