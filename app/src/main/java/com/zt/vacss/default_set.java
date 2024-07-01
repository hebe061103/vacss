package com.zt.vacss;

import static com.zt.vacss.BleClientActivity.receiveData;
import static com.zt.vacss.BleClientActivity.sendData;
import static com.zt.vacss.MainActivity.goAnim;
import static com.zt.vacss.MainActivity.hc06_online;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class default_set extends AppCompatActivity {
    public String TAG = "default_set";

    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_set);
        Intent intent = getIntent();

        Button bt_left = findViewById(R.id.ck_left);
        Button bt_right = findViewById(R.id.ck_right);
        Button click_zz = findViewById(R.id.click_zz);
        Button z_finished = findViewById(R.id.zz_finished);
        bt_left.setOnClickListener(view -> {
            goAnim(default_set.this,30);
            if (hc06_online){
                sendData("<");
                Log.d(TAG, "左移成功");
            }
        });
        bt_right.setOnClickListener(view -> {
            goAnim(default_set.this,30);
            if (hc06_online){
                sendData(">");
                Log.d(TAG, "右移成功");
            }
        });
        click_zz.setOnClickListener(view -> {
            goAnim(default_set.this,30);
            if (hc06_online){
                sendData("9");
                Log.d(TAG, "装针");
            }
        });
        z_finished.setOnClickListener(view -> {
            goAnim(default_set.this,30);
            if (hc06_online) {
                if (intent != null) {
                    String value = intent.getStringExtra("zz");
                    // 使用传递过来的参数
                    if ("no".equals(value)) {//主界面点击了没有装针，跳转到此处
                        if (hc06_online) {
                            sendData("y");
                            receiveData();
                            Log.d(TAG, "装针完成");
                            finish();
                        }
                    }else {
                        sendData("0");
                        Log.d(TAG, "发送装针完成指令");
                        new AlertDialog.Builder(default_set.this)
                                .setTitle("是否要立即启动?")
                                .setPositiveButton("取消", null)
                                .setNegativeButton("启动", (dialog, which) -> {
                                    sendData("y");
                                    receiveData();
                                })
                                .show();
                    }
                }
            }
        });
    }
}
