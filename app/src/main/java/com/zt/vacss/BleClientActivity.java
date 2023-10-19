package com.zt.vacss;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;

/** @noinspection deprecation*/
public class BleClientActivity extends AppCompatActivity implements EasyPermissions.RationaleCallbacks {
    private final List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private static final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private RecyclerView mRecyclerView;
    public static BluetoothSocket mSocket;
    private Button re_scan;
    private ProgressDialog pd;
    private int item_locale;
    public static boolean connect_ok;
    public static short rssi;
    private BlueDeviceItemAdapter mRecycler;
    private void writeDate(String device){
        SharedPreferences sp = getSharedPreferences("data",MODE_PRIVATE);//获取 SharedPreferences对象
        SharedPreferences.Editor editor = sp.edit(); // 获取编辑器对象
        editor.putString("deviceName",device ); // 存入String类型数据
        editor.apply();// 提交数据
    }
    @SuppressLint("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);
        registerBluetoothListener();
        initList();
        re_scan = findViewById(R.id.re_scan);
        re_scan.setOnClickListener(v -> {
            goAnim();
            mDeviceList.clear();
            BlueDeviceItemAdapter mRecycler = new BlueDeviceItemAdapter(mDeviceList, BleClientActivity.this);
            mRecyclerView.setAdapter(mRecycler);
            BleClientActivity.this.searchBluetooth();
        });
        searchBluetooth();
    }
    private void initList() {
        //设置固定大小
        mRecyclerView = findViewById(R.id.rv_device_list);
        mRecyclerView.setHasFixedSize(true);
        //创建线性布局
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecycler = new BlueDeviceItemAdapter(mDeviceList, this);
        mRecyclerView.addItemDecoration(new LinearSpacingItemDecoration(this, 10));//添加间距
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)); //添加分隔线
    }

    @SuppressLint("ObsoleteSdkInt")
    public void searchBluetooth() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.BLUETOOTH_SCAN},100);
            }
        }
        mBluetoothAdapter.startDiscovery();
        re_scan.setText("正在扫描");
        pd = new ProgressDialog(this);
        pd.setMessage("正在扫描,请稍等......");
        pd.show();
        pd.setCancelable(false);
    }
    private void stopDiscovery() {
        pd.dismiss();
        re_scan.setText("重新扫描");
    }

    @SuppressLint("MissingPermission")
    private void foundBlueDevice(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (!mDeviceList.contains(device)) {
            assert device != null;
            if (!(device.getName() == null)) {
                rssi= Objects.requireNonNull(intent.getExtras()).getShort(BluetoothDevice.EXTRA_RSSI);
                mDeviceList.add(device);
                mRecyclerView.setAdapter(mRecycler);
                mRecycler.setRecyclerItemLongClickListener(position -> {
                    BleClientActivity.this.goAnim();
                    item_locale = position;
                    BleClientActivity.this.showPopupMenu(mRecyclerView.getChildAt(position));
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
            if (itemId == R.id.connect_item) {
                goAnim();
                if(!isPaired(mDeviceList.get(item_locale))){
                    mDeviceList.get(item_locale).createBond();
                }
                connectHc06();
            } else if (itemId == R.id.disconnect_item) {
                goAnim();
                try {
                    mSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else if (itemId == R.id.clean_bond){
                goAnim();
                new AlertDialog.Builder(this)
                        .setTitle("取消配对")
                        .setMessage("确定吗?")
                        .setPositiveButton("取消", null)
                        .setNegativeButton("确定", (dialog, which) -> {
                            unpairDevice(mDeviceList.get(item_locale));
                            mDeviceList.clear();
                            searchBluetooth();
                        })
                        .show();
            }else if (itemId == R.id.add_rssi_check) {
                goAnim();
                writeDate(mDeviceList.get(item_locale).getAddress());
            }
            return false;
        });
        popupMenu.show();//显示菜单
    }
    @SuppressLint("MissingPermission")
    public boolean isPaired(BluetoothDevice device) {
        return device.getBondState() == BluetoothDevice.BOND_BONDED;
    }
    /** @noinspection JavaReflectionMemberAccess*/ //反射来调用BluetoothDevice.removeBond取消设备的配对
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            showLog("unpairDevice:"+e.getMessage());
        }
    }
    /**
     * 连接设备
     *
     */
    @SuppressLint("MissingPermission")
    private void connectHc06() {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceList.get(item_locale).getAddress());
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        try {
            mSocket = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(){
            @Override
            public void run() {
                mBluetoothAdapter.cancelDiscovery();
                try {
                    mSocket.connect();
                } catch (IOException e) {
                    try {
                        mSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
                super.run();
            }
        }.start();
    }

    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                showLog("blue action: " + action);
                switch (action) {
                    case BluetoothDevice.ACTION_FOUND:
                        foundBlueDevice(intent);
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        stopDiscovery();
                        break;
                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                        showToast("连接成功");
                        connect_ok=true;
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        showToast("连接失败");
                        connect_ok=false;
                        break;
                }
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
    /**
     * 发送消息
     *
     */
    public static void sendMsg(String message) {
        if (mSocket == null) {
            showLog("sendMsg socket is null");
            return;
        }
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    OutputStream outputStream = mSocket.getOutputStream();
                    outputStream.write(message.getBytes());
                } catch (Exception e) {
                    showLog("error:sendMsgException:{}" + e.getMessage());
                }
            }
        }.start();
    }
    /**
     * 接收数据
     */
    public static void receiverData() {
        if (mSocket == null) {
            showLog("Receiver socket is null");
            return;
        }
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    InputStream inputStream = mSocket.getInputStream();
                    byte[] buffer = new byte[1024];
                    int length = inputStream.read(buffer);
                    String message = new String(buffer, 0, length);

                } catch (Exception e) {
                    showLog("error: ReceiverMsgException : {}" + e.getMessage());
                }
            }
        }.start();
    }
    /**
     * 重写方法将权限请求结果传递给EasyPermission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 回调结果传递给EasyPermission
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onRationaleAccepted(int requestCode) {
        showLog("权限申请成功!");
    }

    @Override
    public void onRationaleDenied(int requestCode) {
        showLog("用户巳拒绝权限申请!");
        finish();
    }
    //抖动震动
    protected void goAnim(){
        // 震动效果的系统服务
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(20);//振动0.5秒
        // 下边是可以使震动有规律的震动  -1：表示不重复 0：循环的震动
    }
    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    private static void showLog(String text){
        Log.d("BleClientActivity:", "showLog: " + text);
    }
    protected void onDestroy() {
        unregisterReceiver(mBluetoothReceiver);
        super.onDestroy();
    }
}
