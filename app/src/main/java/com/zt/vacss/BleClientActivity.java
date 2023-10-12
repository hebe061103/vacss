package com.zt.vacss;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/** @noinspection deprecation*/
public class BleClientActivity extends AppCompatActivity {
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private List<BluetoothDevice> mDeviceList;
    private static final int BLUE_START_STATE = 1;
    private static final int BLUE_ACTION_DISCOVERY_STARTED = 2;
    private static final int BLUE_ACTION_DISCOVERY_FINISHED = 3;
    private Thread mToastThread;
    private boolean isScan_ing = false;
    private RecyclerView mRecyclerView;

    private void getBlueState(int blueState) {
        switch (blueState) {
            case BluetoothAdapter.STATE_TURNING_ON:
                showLog("蓝牙正在打开");
                break;
            case BluetoothAdapter.STATE_ON:
                showLog("蓝牙已经打开");
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                showLog("蓝牙正在关闭");
                break;
            case BluetoothAdapter.STATE_OFF:
                showLog("蓝牙已经关闭");
                break;
        }
    }

    private final Handler mHandler = new Handler(message -> {
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
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //监听蓝牙关闭和打开状态
        filter.addAction(BluetoothDevice.ACTION_FOUND); //搜索发现设备
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //搜索完毕
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED); //配对状态
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);//蓝牙连接成功
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED); //蓝牙连接失败
        registerReceiver(mBluetoothReceiver, filter);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);
        mRecyclerView = findViewById(R.id.rv_device_list);
        Button click_scan = findViewById(R.id.click_scan);
        click_scan.setOnClickListener(view -> searchBluetooth());
        registerBluetoothListener();
        initList();
        searchBluetooth();
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
        BlueDeviceItemAdapter mListAdapter = new BlueDeviceItemAdapter(mDeviceList, this);
        mRecyclerView.setAdapter(mListAdapter);
    }

    public void searchBluetooth() {
        if (mBluetoothAdapter == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
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

    /** @noinspection BusyWait*/
    private void actionDiscovery() {
        mToastThread = new Thread(() -> {
            isScan_ing = true;
            while (isScan_ing) {
                try {
                    showToast("扫描中......");
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
        Log.d(TAG, "己配对设备数量: =" + devices.size());
        for (BluetoothDevice bondDevice : devices) {
            Log.d(TAG, "己配对设备名: =" + bondDevice.getName() + "设备地址:" + bondDevice.getAddress());
        }
    }

    private void foundBlueDevice(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (device != null) {
            if (!mDeviceList.contains(device)){
                mDeviceList.add(device);
            }
        }
    }
    private void showLog(String text){
        Log.d(TAG, "showLog: "+text);
    }
    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    protected void onDestroy() {
        isScan_ing = false;
        mToastThread = null;
        unregisterReceiver(mBluetoothReceiver);
        super.onDestroy();
    }
}
