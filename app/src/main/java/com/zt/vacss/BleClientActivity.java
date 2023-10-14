package com.zt.vacss;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


/** @noinspection deprecation*/
public class BleClientActivity extends AppCompatActivity {
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private List<BluetoothDevice> mDeviceList;
    private static final int BLUE_START_STATE = 1;
    private static final int BLUE_ACTION_DISCOVERY_STARTED = 2;
    private static final int BLUE_ACTION_DISCOVERY_FINISHED = 3;
    private RecyclerView mRecyclerView;
    private Button re_scan;
    private Boolean scanStatus;
    private ProgressDialog pd;
    private int item_locale;

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
                searchBluetooth();
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
        re_scan = findViewById(R.id.re_scan);
        re_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goAnim();
                if (!scanStatus) {
                    mDeviceList.clear();
                    BlueDeviceItemAdapter mRecycler = new BlueDeviceItemAdapter(mDeviceList, BleClientActivity.this);
                    mRecyclerView.setAdapter(mRecycler);
                    BleClientActivity.this.searchBluetooth();}
            }
        });
        registerBluetoothListener();
        initList();
        searchBluetooth();
    }

    private void initList() {
        mDeviceList = new ArrayList<>();
        //设置固定大小
        mRecyclerView.setHasFixedSize(true);
        //创建线性布局
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
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
        scanStatus=true;
        mBluetoothAdapter.startDiscovery();
        re_scan.setText("正在扫描");
        pd = new ProgressDialog(this);
        pd.setMessage("正在扫描,请稍等......");
        pd.show();
        pd.setCancelable(false);
    }

    private void stopDiscovery() {
        scanStatus=false;
        pd.dismiss();
        re_scan.setText("重新扫描");
    }

    @SuppressLint("MissingPermission")
    private void foundBlueDevice(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (!mDeviceList.contains(device)) {
            assert device != null;
            if (!(device.getName() == null)) {
                mDeviceList.add(device);
                mRecyclerView.addItemDecoration(new LinearSpacingItemDecoration(this, 10));
                BlueDeviceItemAdapter mRecycler = new BlueDeviceItemAdapter(mDeviceList, this);
                mRecyclerView.setAdapter(mRecycler);
                mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)); //添加分隔线
                mRecycler.setRecyclerItemLongClickListener(new BlueDeviceItemAdapter.OnRecyclerItemLongClickListener() {
                    @Override
                    public void onRecyclerItemLongClickListener(int postion) {
                        goAnim();
                        item_locale=postion;
                        showPopupMenu(mRecyclerView);
                    }
                });
            }
        }
    }
    @SuppressLint("MissingPermission")
    private void showPopupMenu(final View view) {
        final PopupMenu popupMenu = new PopupMenu(this, view, Gravity.END);
        //menu 布局
        popupMenu.getMenuInflater().inflate(R.menu.connectmenu, popupMenu.getMenu());
        //点击事件
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bond_item) {
                goAnim();
                mDeviceList.get(item_locale).createBond();
            }else if (itemId == R.id.connect_item) {
                goAnim();
            } else if (itemId == R.id.disconnect_item) {
                goAnim();
            }
            return false;
        });
        //显示菜单，不要少了这一步
        popupMenu.show();
    }

    //抖动震动
    protected void goAnim(){
        // 震动效果的系统服务
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(40);//振动0.5秒
        // 下边是可以使震动有规律的震动  -1：表示不重复 0：循环的震动
    }
    private void showLog(String text){
        Log.d("vsLog", "showLog: " + text);
    }
    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    protected void onDestroy() {
        unregisterReceiver(mBluetoothReceiver);
        super.onDestroy();
    }
}
