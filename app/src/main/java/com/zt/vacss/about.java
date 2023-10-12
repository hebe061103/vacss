package com.zt.vacss;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class about extends AppCompatActivity {
    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        TextView mBlueMessage = findViewById(R.id.blue_info);
        mBlueMessage.setText(getBluetoothMAC(this));
    }
    @SuppressWarnings("MissingPermission")
    public static String getBluetoothMAC(Context context) {
        String info = null;
        try {
            if (context.checkCallingOrSelfPermission(android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
                info = "name:"+ bta.getName()+"\n"+"address:"+bta.getAddress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info ;
    }

}
