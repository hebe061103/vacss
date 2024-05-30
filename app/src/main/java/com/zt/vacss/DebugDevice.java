package com.zt.vacss;

import static com.zt.vacss.BleClientActivity.sendData;
import static com.zt.vacss.MainActivity.get51Data;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class DebugDevice extends AppCompatActivity {
    Button mLeftDel,mLeftAdd,mRightDel,mRightAdd,mLeft_time_add,mLeft_time_del,mRight_time_add,mRight_time_del;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_device);
        initButton();

    }

    private void initButton() {
        mLeftDel = findViewById(R.id.leftDel);
        mLeftDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData("2");
            }
        });
        mLeftAdd = findViewById(R.id.leftAdd);
        mLeftAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData("1");
            }
        });
        mRightDel = findViewById(R.id.rightDel);
        mRightDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData("4");
            }
        });
        mRightAdd = findViewById(R.id.rightAdd);
        mRightAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData("3");
            }
        });
        mLeft_time_del=findViewById(R.id.left_time_del);
        mLeft_time_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData("5");
            }
        });
        mLeft_time_add=findViewById(R.id.left_time_add);
        mLeft_time_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData("6");
            }
        });
        mRight_time_del=findViewById(R.id.right_time_del);
        mRight_time_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData("7");
            }
        });
        mRight_time_add=findViewById(R.id.right_time_add);
        mRight_time_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData("8");
            }
        });
    }
    protected void onDestroy() {
        super.onDestroy();
        get51Data();
    }

}
