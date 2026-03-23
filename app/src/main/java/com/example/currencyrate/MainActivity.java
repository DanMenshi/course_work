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

        ViewGroup gridLayout = findViewById(R.id.glCurrencies);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View card = gridLayout.getChildAt(i);
            card.setOnClickListener(v -> {
                animateClick(v);
                
                // Извлекаем данные из карточки для передачи в детали
                String code = "USD";
                String rate = "0.00";
                
                if (v instanceof ViewGroup) {
                    ViewGroup vg = (ViewGroup) v;
                    // В нашем layout структура: FrameLayout -> LinearLayout -> TextViews
                    // Мы можем найти TextView с кодом валюты
                    for (int j = 0; j < vg.getChildCount(); j++) {
                        View child = vg.getChildAt(j);
                        if (child instanceof ViewGroup) {
                            ViewGroup innerVg = (ViewGroup) child;
                            for (int k = 0; k < innerVg.getChildCount(); k++) {
                                View text = innerVg.getChildAt(k);
                                if (text instanceof TextView) {
                                    String content = ((TextView) text).getText().toString();
                                    if (content.contains("/")) code = content.split("/")[0];
                                    if (Character.isDigit(content.charAt(0))) rate = content;
                                }
                            }
                        }
                    }
                }

                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("CURRENCY_CODE", code);
                intent.putExtra("CURRENCY_RATE", rate);
                startActivity(intent);
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