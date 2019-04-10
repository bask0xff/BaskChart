package ru19july.tgchart;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

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
                startActivity(intent);
            }
        });

        btnCnv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.putExtra("chart_type", 1);
                startActivity(intent);
            }
        });
    }
}
