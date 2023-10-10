package com.zt.vacss;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.Set;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class BleClientActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    private BluetoothAdapter mBluetoothAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startBluetoothScan();
    }

    private void startBluetoothScan() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showToast("本设备没有蓝牙功能");
            return;
        }

        //先判断有没有权限
        if ((ActivityCompat.checkSelfPermission(this, "Manifest.permission.BLUETOOTH") != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, "Manifest.permission.BLUETOOTH_ADMIN") != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, "Manifest.permission.ACCESS_COARSE_LOCATION") != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, "Manifest.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED)) {
            requestBluetoothPermissions();
        }
    }

    private void requestBluetoothPermissions() {
        requestPermissions(new String[]{
                "Manifest.permission.BLUETOOTH",
                "Manifest.permission.BLUETOOTH_ADMIN",
                "Manifest.permission.ACCESS_COARSE_LOCATION",
                "Manifest.permission.ACCESS_FINE_LOCATION"
        }, 200);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "允许", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "禁止", Toast.LENGTH_SHORT).show();
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
            //弹出个对话框 可以自定义
        }
    }

    private void startScan() {
        //使自身蓝牙可见
        if (mBluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
                startActivity(discoverableIntent);
            }
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        Log.d(TAG, "bonded device size ="+devices.size());
        for(BluetoothDevice bonddevice:devices){
            Log.d(TAG, "bonded device name ="+bonddevice.getName()+" address"+bonddevice.getAddress());
        }
    }
    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


}
