package com.zt.vacss;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Set;

/** @noinspection deprecation*/
public class bond_bt extends AppCompatActivity {
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final List<String> mbondlist=null;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bond_bt);
        ListView mListView = findViewById(R.id.bond_list);
        getPairedDevices();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,mbondlist);
        mListView.setAdapter(arrayAdapter);
    }

    @SuppressLint("MissingPermission")
    private void getPairedDevices() {
        @SuppressLint("MissingPermission") Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        Log.d("pd", "己配对设备数量: =" + devices.size());
        for (BluetoothDevice bondDevice : devices) {
            Log.d("pd", "己配对设备名: =" + bondDevice.getName() + "设备地址:" + bondDevice.getAddress());
            String bd = bondDevice.getName()+"\n"+bondDevice.getAddress();

        }
    }
}
