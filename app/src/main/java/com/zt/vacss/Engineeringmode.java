package com.zt.vacss;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Engineeringmode extends AppCompatActivity {
    Button button_server;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mode_engineering);
        button_server=findViewById(R.id.server_mode);
        initClick();
    }

    private void initClick () {
        button_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Engineeringmode.this,BleServerActivity.class);
                startActivities(new Intent[]{intent});
            }
        });
    }
}
