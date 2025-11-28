package com.example.mycoursework;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;


public class RatesActivity extends AppCompatActivity {

    private TextView sumUsd, sumEur, sumCny;
    private Handler handler = new Handler();
    private Runnable updateTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rates);

        sumUsd = findViewById(R.id.sumUsd);
        sumEur = findViewById(R.id.sumEur);
        sumCny = findViewById(R.id.sumCny);

        findViewById(R.id.btnManualRefresh).setOnClickListener(v -> updateRates());

        updateTask = () -> {
            updateRates();
            handler.postDelayed(updateTask, 10000);// 10 секунд
            Toast.makeText(RatesActivity.this, "Обновление данных", Toast.LENGTH_SHORT).show();
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(updateTask);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateTask);
    }

    private void updateRates() {
        int usd = 70 + (int)(Math.random() * (90 - 71));
        int eur = 70 + (int)(Math.random() * (90 - 71));
        int cny = 70 + (int)(Math.random() * (90 - 71));
        sumUsd.setText("~" + usd);
        sumEur.setText("~" + eur);
        sumCny.setText("~" + cny);
    }
}