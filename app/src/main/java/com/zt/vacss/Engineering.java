
package com.zt.vacss;

import static com.zt.vacss.BleClientActivity.connect_ok;
import static com.zt.vacss.BleClientActivity.debugData;
import static com.zt.vacss.BleClientActivity.receiveData;
import static com.zt.vacss.BleClientActivity.sendData;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Engineering extends AppCompatActivity {
    public static String TAG = "Engineering";
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
            Intent intent = new Intent(Engineering.this,BleServerActivity.class);
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
                sendEditProcess();
                debugData=null;
                displayData(jieShou);
            }
        });
    }

    private void sendEditProcess() {
        ck_sent.setOnClickListener(view -> {
            String sendStr = faSong.getText().toString();
            Log.d(TAG, "sendStr:"+sendStr);
            if(!sendStr.isEmpty()) {
                sendData(sendStr);
                receiveData();
                faSong.setText("");
                Toast.makeText(this, "巳发送", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "请不要发送空数据", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void displayData(TextView s){
        new Thread(){
            /** @noinspection InfiniteLoopStatement*/
            public void run(){
                while (true){
                    if(debugData!=null) {
                        s.setText(debugData);
                        debugData=null;
                    }
                }
            }
        }.start();
    }
    protected void onDestroy() {
        super.onDestroy();
    }
}
