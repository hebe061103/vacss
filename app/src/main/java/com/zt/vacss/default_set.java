package com.zt.vacss;

import static com.zt.vacss.BleClientActivity.sendData;
import static com.zt.vacss.MainActivity.goAnim;
import static com.zt.vacss.MainActivity.hc06_online;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class default_set extends AppCompatActivity {
    public String TAG = "default_set";

    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_set);

        Button bt_left = findViewById(R.id.ck_left);
        Button bt_right = findViewById(R.id.ck_right);

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
    }
}
