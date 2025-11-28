package com.example.mycoursework;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button btnOpenRates = findViewById(R.id.btnRates);
        Button btnDummy = findViewById(R.id.btnAbout);


        btnOpenRates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, RatesActivity.class);
                startActivity(i);
            }
        });


        btnDummy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Обновление (заглушка)", Toast.LENGTH_SHORT).show();
            }
        });
    }
}