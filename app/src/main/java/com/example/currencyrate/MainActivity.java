package com.example.currencyrate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.view.ViewGroup;

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

        // Находим сетку и вешаем клики на все карточки валют
        ViewGroup gridLayout = findViewById(R.id.glCurrencies);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View card = gridLayout.getChildAt(i);
            card.setOnClickListener(v -> {
                animateClick(v);
                startActivity(new Intent(MainActivity.this, DetailsActivity.class));
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