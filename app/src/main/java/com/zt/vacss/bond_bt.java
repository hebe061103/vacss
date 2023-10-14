package com.zt.vacss;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class bond_bt extends AppCompatActivity {
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static List<BluetoothDevice> mList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bond_bt);
        mList = new ArrayList<>();
        getPairedDevices();
        ListView mListView = findViewById(R.id.bond_list);
        ArrayAdapter<BluetoothDevice> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mList);
        mListView.setAdapter(arrayAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                new AlertDialog.Builder(bond_bt.this)
                        .setTitle("取消配对")
                        .setMessage("确定吗?")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onClick(DialogInterface dialogInterface, int b) {
                                Toast.makeText(bond_bt.this,mList.get((int) l).getName(), Toast.LENGTH_SHORT).show();
                                unpairDevice(mList.get((int) l));
                            }
                        })
                        .setNegativeButton("否", null)
                        .show();
            }
        });
    }
    @SuppressLint("MissingPermission")
    private void getPairedDevices() {
        @SuppressLint("MissingPermission") Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        Log.d("pd", "己配对设备数量: =" + devices.size());
        for (BluetoothDevice bondDevice : devices) {
            Log.d("pd", "己配对设备名: =" + bondDevice.getName() + "设备地址:" + bondDevice.getAddress());
            mList.add(bondDevice);
        }
    }

    //反射来调用BluetoothDevice.removeBond取消设备的配对
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e("vslog", e.getMessage());
        }
    }
}
