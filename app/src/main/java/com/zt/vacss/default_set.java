package com.zt.vacss;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class default_set extends AppCompatActivity {
Button bt_left,bt_right;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_set);

        bt_left = findViewById(R.id.ck_left);
        bt_right = findViewById(R.id.ck_right);


    }
}
