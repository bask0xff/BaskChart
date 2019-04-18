package ru19july.baskchart;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

public class StartActivity extends AppCompatActivity {

    int chartNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button btnGl = findViewById(R.id.btn_opengl);
        Button btnCnv = findViewById(R.id.btn_canvas);

        btnGl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.putExtra("chart_type", 0);
                intent.putExtra("chart_number", chartNumber);
                startActivity(intent);
            }
        });

        btnCnv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.putExtra("chart_type", 1);
                intent.putExtra("chart_number", chartNumber);
                startActivity(intent);
            }
        });

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        radioGroup.clearCheck();

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case -1:
                        break;
                    case R.id.radio1:
                        chartNumber = 1;
                        break;
                    case R.id.radio2:
                        chartNumber = 2;
                        break;
                    case R.id.radio3:
                        chartNumber = 3;
                        break;
                    case R.id.radio4:
                        chartNumber = 4;
                        break;
                    case R.id.radio5:
                        chartNumber = 5;
                        break;
                }
            }
        });
    }
}