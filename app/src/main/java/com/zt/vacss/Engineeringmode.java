package com.zt.vacss;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Engineeringmode extends AppCompatActivity {
    Button button_server;
    Button hc06_btn;
    private TextView jieShou;
    private EditText faSong;
    private Button ck_sent;
    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mode_engineering);
        button_server=findViewById(R.id.server_mode);
        jieShou = findViewById(R.id.jieShou);
        faSong = findViewById(R.id.fasong);
        ck_sent = findViewById(R.id.ck_sent);
        hc06_btn = findViewById(R.id.hc06_btn);
        jieShou.setHint("点击上方HC6测试连接");
        faSong.setEnabled(false);
        ck_sent.setEnabled(false);
        initClick();
    }

    private void initClick () {
        button_server.setOnClickListener(view -> {
            Intent intent = new Intent(Engineeringmode.this,BleServerActivity.class);
            startActivities(new Intent[]{intent});
        });
        hc06_btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if(!BleClientActivity.connect_ok) {
                    jieShou.setHint("未连接HC6:");
                }else {
                    jieShou.setHint("巳准备好接收:");
                    faSong.setEnabled(true);
                    ck_sent.setEnabled(true);
                    sendEditProcess();
                }
            }
        });
    }

    private void sendEditProcess() {
        ck_sent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sendEdit = faSong.getText().toString();
                if(!sendEdit.isEmpty()) {
                    BleClientActivity.sendMsg(sendEdit);
                }
            }
        });
    }
}
