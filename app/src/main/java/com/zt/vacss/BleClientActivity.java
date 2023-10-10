package com.zt.vacss;

import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class BleClientActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "第二个页面" , Toast.LENGTH_SHORT).show();
    }
    private void startBluetoothScan() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showToast("本设备没有蓝牙功能");
            return;
        }

        //先判断有没有权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                ||ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                ||ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ||ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showToast("没有给蓝牙权限");
            requestBluetoothPermissions();
            return;
        }

        //判断及启动 位置信息
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //判断是否开启了GPS
        boolean ok = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!ok){
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent,LOCATION_START_RECODE);
        }

        //判断及启动 蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            BleClientActivity.this.startActivityForResult(intent, BLUE_START_RECODE);
//            mBluetoothAdapter.enable();
        } else {
            startScan();
        }
    }
    private void requestBluetoothPermissions() {
        requestPermissions(new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, BLUE_PERMISSIONS_RECODE);
    }
    private void startScan() {
        //使自身蓝牙可见
        if (mBluetoothAdapter.isEnabled()) {
            if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
                startActivity(discoverableIntent);
            }
        }

        //展示自身蓝牙信息
        if (mBlueMessage != null && mBluetoothAdapter != null) {
            String name = mBluetoothAdapter.getName();
            String address = mBluetoothAdapter.getAddress();
//            String uuid = UUID.randomUUID().toString();
            mBlueMessage.setText("name : " + name +" address: " + address);
//            mBlueMessage.setText(uuid);
//            showLog("UUID : " + uuid);
        }

        //开始扫描
        if (mBluetoothAdapter != null) {
            // 可以同时发现 经典蓝牙 和 ble 的
            mBluetoothAdapter.startDiscovery();
//            mBluetoothAdapter.setName("pun client");
        }

        getPairedDevices();
    }

    private void getPairedDevices() {
        //获取已经配对的蓝牙设备
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        Log.d(TAG, "bonded device size ="+devices.size());
        for(BluetoothDevice bonddevice:devices){
            Log.d(TAG, "bonded device name ="+bonddevice.getName()+" address"+bonddevice.getAddress());
        }
    }

    private void actionDiscovery() {
        mToastThread = new Thread(new Runnable() {
            @Override
            public void run() {
                isScaning = true;
                while(isScaning){
                    try {
                        showToast("扫描中...");
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mToastThread.start();
    }

    private void stopDiscovery() {
        isScaning = false;
        mToastThread = null;
        showToast("扫描完毕!");
    }
    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
