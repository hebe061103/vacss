package com.zt.vacss;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class OptionSetting extends AppCompatActivity {
public static TextView always_ck;
public String checkName;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.option_set);
        always_ck = findViewById(R.id.always_check_name);
        checkName=getIntent().getStringExtra("checkName");
        always_ck.setText(checkName);
        ck_listen();
    }
    private void ck_listen(){
        always_ck.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(OptionSetting.this)
                        .setTitle("取消持续检测吗?")
                        .setMessage("确定吗?")
                        .setPositiveButton("取消", null)
                        .setNegativeButton("确定", (dialog, which) -> {
                            always_ck.setText("");
                            Intent intent = new Intent(OptionSetting.this,RefreshRssi.class);
                            stopService(intent);
                        })
                        .show();
                return true;
            }
        });
    }
}