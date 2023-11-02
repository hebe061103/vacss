package com.zt.vacss;

import static com.zt.vacss.BleClientActivity.connect_ok;
import static com.zt.vacss.BleClientActivity.inputData;
import static com.zt.vacss.BleClientActivity.receiveData;
import static com.zt.vacss.BleClientActivity.sendData;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
        hc06_btn.setOnClickListener(v -> {
            if(!connect_ok) {
                jieShou.setHint("未连接HC6:");
            }else {
                jieShou.setHint("巳准备好接收:");
                hc06_btn.setEnabled(false);
                faSong.setEnabled(true);
                ck_sent.setEnabled(true);
                receiveData();
                sendEditProcess();
                displayData();
            }
        });
    }

    private void sendEditProcess() {
        ck_sent.setOnClickListener(view -> {
            String sendEdit = faSong.getText().toString();
            if(!sendEdit.isEmpty()) {
                sendData(sendEdit);
                faSong.setText("");
                Toast.makeText(Engineeringmode.this, "巳发送", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void displayData(){
        new Thread(){
            /** @noinspection InfiniteLoopStatement*/
            public void run(){
                while (true){
                    if(inputData!=null) {
                        jieShou.setText(inputData);
                    }
                }
            }
        }.start();
    }
    protected void onDestroy() {
        super.onDestroy();
    }
}
