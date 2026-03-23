package com.example.currencyrate;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
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

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupChart();
        setupButtonAnimations();
    }

    private void setupChart() {
        LineChart chart = findViewById(R.id.chart);
        
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 92.21f));
        entries.add(new Entry(1, 93.80f));
        entries.add(new Entry(2, 91.50f));
        entries.add(new Entry(3, 92.10f));
        entries.add(new Entry(4, 93.40f));
        entries.add(new Entry(5, 91.00f));
        entries.add(new Entry(6, 91.10f));

        LineDataSet dataSet = new LineDataSet(entries, "USD Rate");
        
        // Современный стиль линии
        dataSet.setColor(Color.parseColor("#1E88E5"));
        dataSet.setLineWidth(3f);
        dataSet.setDrawCircles(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Плавная линия как на макете
        
        // Градиентная заливка под графиком
        dataSet.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.chart_gradient);
        dataSet.setFillDrawable(drawable);
        
        dataSet.setDrawValues(false);
        dataSet.setHighlightEnabled(true);
        dataSet.setHighLightColor(Color.WHITE);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        
        // Настройка осей для чистого вида
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
        chart.setExtraOffsets(10, 10, 10, 10);
        
        // Анимация при открытии
        chart.animateX(1200);
    }

    private void setupButtonAnimations() {
        View refreshBtn = findViewById(R.id.btnRefresh);
        refreshBtn.setOnClickListener(v -> {
            // Анимация вращения и пульсации
            v.animate().rotationBy(360).scaleX(0.8f).scaleY(0.8f).setDuration(300).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
            }).start();
        });
        
        // Пример анимации для табов
        View tabs = findViewById(R.id.llTabs);
        for (int i = 0; i < ((android.view.ViewGroup)tabs).getChildCount(); i++) {
            View tab = ((android.view.ViewGroup)tabs).getChildAt(i);
            tab.setOnClickListener(v -> {
                animateClick(v);
                // Логика переключения периода
            });
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