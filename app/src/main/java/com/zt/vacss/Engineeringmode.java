package com.zt.vacss;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Engineeringmode extends AppCompatActivity {
    Button button_server;
    Button hc06_btn;
    private TextView jieShou;
    private TextView faSong;
    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mode_engineering);
        button_server=findViewById(R.id.server_mode);
        jieShou = findViewById(R.id.jieShou);
        faSong = findViewById(R.id.faSong);
        hc06_btn = findViewById(R.id.hc06_btn);
        jieShou.setText("点击上方HC6测试连接");
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
        hc06_btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if(!BleClientActivity.connect_ok) {
                    jieShou.setText("巳准备好接收:");
                    faSong.setText("可以发送了:");
                }else {
                    jieShou.setText("未连接HC6:");
                    faSong.setText("未连接HC6");
                }
            }
        });
    }
}
