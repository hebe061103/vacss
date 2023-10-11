package com.zt.vacss;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/** @noinspection deprecation*/
public class BleClientActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothDevice> mDeviceList;
    private BlueDeviceItemAdapter mListAdapter;
    private static final int BLUE_START_STATE = 1;
    private static final int BLUE_ACTION_DISCOVERY_STARTED = 2;
    private static final int BLUE_ACTION_DISCOVERY_FINISHED = 3;
    private Thread mToastThread;
    private boolean isScan_ing = false;
    private RecyclerView mRecyclerView;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
            //弹出个对话框 可以自定义
        }
    }

    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case BleClientActivity.BLUE_START_STATE:
                    getBlueState(message.arg1);
                    break;
                case BleClientActivity.BLUE_ACTION_DISCOVERY_STARTED:
                    actionDiscovery();
                    break;
                case BleClientActivity.BLUE_ACTION_DISCOVERY_FINISHED:
                    stopDiscovery();
                    break;
                default:
                    break;
            }
            return false;
        }
    });
    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Message msg = new Message();
            if (action != null) {
                showLog("blue action: " + action);
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        msg.what = BLUE_START_STATE;
                        msg.arg1 = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        msg.what = BLUE_ACTION_DISCOVERY_STARTED;
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        foundBlueDevice(intent);
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        msg.what = BLUE_ACTION_DISCOVERY_FINISHED;
                        break;
                }
                mHandler.sendMessage(msg);
            }
        }
    };

    private void registerBluetoothListener() {
        IntentFilter filter = new IntentFilter();
        //开关状态
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //开始查找
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //结束查找
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //查找设备
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        //设备扫描模式变化
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        //绑定状态
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBluetoothReceiver, filter);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRecyclerView = findViewById(R.id.rv_device_list);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        registerBluetoothListener();
        initList();
    }
    private void initList() {
        //设置固定大小
        mRecyclerView.setHasFixedSize(true);
        //创建线性布局
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        //创建适配器，并且设置
        mDeviceList = new ArrayList<>();
        mListAdapter = new BlueDeviceItemAdapter(mDeviceList);
        mRecyclerView.setAdapter(mListAdapter);
    }
    private void startBluetoothScan() {
        if (mBluetoothAdapter == null) {
            showToast("本设备没有蓝牙功能");
            return;
        }
        if ((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, 300);
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 200);
        } else {
            startScan();
        }
    }


    private void startScan() {
        //开始扫描
        if (mBluetoothAdapter != null) {
            // 可以同时发现 经典蓝牙 和 ble 的
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
            mBluetoothAdapter.startDiscovery();
        }

        getPairedDevices();
    }
    private void actionDiscovery() {
        mToastThread = new Thread(new Runnable() {
            @Override
            public void run() {
                isScan_ing = true;
                while(isScan_ing){
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
        isScan_ing = false;
        mToastThread = null;
        showToast("扫描完毕!");
    }
    private void getBlueState(int blueState) {
        switch (blueState) {
            case BluetoothAdapter.STATE_TURNING_ON:
                showLog("蓝牙正在打开");
                break;
            case BluetoothAdapter.STATE_ON:
                showLog("蓝牙已经打开");
                startScan();
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                showLog("蓝牙正在关闭");
                break;
            case BluetoothAdapter.STATE_OFF:
                showLog("蓝牙已经关闭");
                break;
        }
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
        Log.d(TAG, "bonded device size =" + devices.size());
        for (BluetoothDevice bonddevice : devices) {
            Log.d(TAG, "bonded device name =" + bonddevice.getName() + " address" + bonddevice.getAddress());
        }
    }

    private void foundBlueDevice(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (device != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            showLog("device name --> " + device.getName());
            if (!mDeviceList.contains(device)){
                mDeviceList.add(device);
                mListAdapter.setItems(mDeviceList);
            }
        }
    }
    private void showLog(String text) {
        showLog(text);
    }
    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    protected void onDestroy() {
        isScan_ing = false;
        mToastThread = null;
        super.onDestroy();
    }
}
