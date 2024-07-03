package com.zt.vacss;

import static com.zt.vacss.BleClientActivity.receiveData;
import static com.zt.vacss.BleClientActivity.sendData;
import static com.zt.vacss.MainActivity.goAnim;
import static com.zt.vacss.MainActivity.hc06_online;

import android.annotation.SuppressLint;
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
        Button bt_left = findViewById(R.id.ck_left);
        Button bt_right = findViewById(R.id.ck_right);
        Button click_zz = findViewById(R.id.click_zz);
        Button close_needle = findViewById(R.id.close_needle);
        Button z_finished = findViewById(R.id.zz_finished);
        bt_left.setOnClickListener(view -> {
            goAnim(default_set.this,30);
            Log.d(TAG, "发送左移指令");
            if (hc06_online){
                sendData("<");
            }
        });
        bt_right.setOnClickListener(view -> {
            goAnim(default_set.this,30);
            Log.d(TAG, "发送右移指令");
            if (hc06_online){
                sendData(">");
            }
        });
        click_zz.setOnClickListener(view -> {
            goAnim(default_set.this,30);
            Log.d(TAG, "发送装针指令");
            if (hc06_online){
                sendData("9");
            }
        });
        close_needle.setOnClickListener(view -> {
            goAnim(default_set.this,30);
            Log.d(TAG, "发送压针指令");
            if (hc06_online){
                sendData("`");
            }
        });

        z_finished.setOnClickListener(view -> {
            goAnim(default_set.this,30);
            Log.d(TAG, "发送装针完成指令");
            if (hc06_online) {
                new AlertDialog.Builder(default_set.this)
                    .setTitle("是否要立即启动?")
                    .setPositiveButton("取消", null)
                    .setNegativeButton("启动", (dialog, which) -> {
                        sendData("y");
                        receiveData();
                    })
                    .show();
            }
        });
    }
}
