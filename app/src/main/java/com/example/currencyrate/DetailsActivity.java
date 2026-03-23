package com.example.currencyrate;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class DetailsActivity extends AppCompatActivity {

    private LineChart chart;
    private TextView tvCurrencyCode, tvCurrencyName, tvCurrentRate, tvTrend, tvUnit;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        chart = findViewById(R.id.chart);
        tvCurrencyCode = findViewById(R.id.tvCurrencyCode);
        tvCurrencyName = findViewById(R.id.tvCurrencyName);
        tvCurrentRate = findViewById(R.id.tvCurrentRate);
        tvTrend = findViewById(R.id.tvTrend);
        tvUnit = findViewById(R.id.tvUnit);

        String code = getIntent().getStringExtra("CURRENCY_CODE");
        if (code == null) code = "USD";
        
        Currency currency = CurrencyData.getByCode(code);

        tvCurrencyCode.setText(currency.getCode());
        tvCurrencyName.setText(currency.getName());
        tvCurrentRate.setText(String.format(Locale.getDefault(), "%.2f %s", currency.getRate(), "₽"));
        tvUnit.setText(String.format(Locale.getDefault(), "за 1 %s", currency.getCode()));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupChart();
        updateRandomData(currency.getRate());
        setupButtonAnimations();
    }

    private void setupChart() {
        chart.getAxisLeft().setTextColor(Color.parseColor("#757575"));
        chart.getAxisLeft().setGridColor(Color.parseColor("#22FFFFFF"));
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getAxisRight().setEnabled(false);
        
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#757575"));
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final String[] days = {"18.3", "19.3", "20.3", "21.3", "22.3", "23.3", "24.3"};
            @Override
            public String getFormattedValue(float value) {
                return days[(int) value % days.length];
            }
        });

        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
    }

    private void updateRandomData(double currentRate) {
        List<Entry> entries = new ArrayList<>();
        float baseRate = (float) currentRate;
        for (int i = 0; i < 7; i++) {
            float val = baseRate + (random.nextFloat() * (baseRate * 0.04f) - (baseRate * 0.02f));
            entries.add(new Entry(i, val));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Rate");
        dataSet.setColor(Color.parseColor("#1E88E5"));
        dataSet.setLineWidth(3f);
        dataSet.setDrawCircles(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.chart_gradient);
        dataSet.setFillDrawable(drawable);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.animateX(1000);

        // Random trend
        boolean up = random.nextBoolean();
        float percent = 0.1f + random.nextFloat() * 1.5f;
        tvTrend.setText(String.format(Locale.getDefault(), "%s %.2f%%", (up ? "↗ +" : "↘ -"), percent));
        tvTrend.setTextColor(up ? Color.parseColor("#00C853") : Color.parseColor("#FF1744"));
    }

    private void setupButtonAnimations() {
        findViewById(R.id.btnRefresh).setOnClickListener(v -> {
            v.animate().rotationBy(360).scaleX(0.8f).scaleY(0.8f).setDuration(300).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                Currency c = CurrencyData.getByCode(tvCurrencyCode.getText().toString());
                updateRandomData(c.getRate());
            }).start();
        });

        View tabs = findViewById(R.id.llTabs);
        if (tabs instanceof android.view.ViewGroup) {
            android.view.ViewGroup vg = (android.view.ViewGroup) tabs;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View tab = vg.getChildAt(i);
                tab.setOnClickListener(v -> {
                    animateClick(v);
                    Currency c = CurrencyData.getByCode(tvCurrencyCode.getText().toString());
                    updateRandomData(c.getRate());
                });
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
