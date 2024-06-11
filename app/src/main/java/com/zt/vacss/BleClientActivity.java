package com.zt.vacss;

import static com.zt.vacss.MainActivity.deleteData;
import static com.zt.vacss.MainActivity.discoveryFinished;
import static com.zt.vacss.MainActivity.goAnim;
import static com.zt.vacss.MainActivity.hc06_online;
import static com.zt.vacss.MainActivity.mlist;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

/** @noinspection deprecation*/
public class BleClientActivity extends AppCompatActivity {
    public static String TAG = "BleClientActivity";
    public static String inputData;
    public static String debugData;
    public static ByteArrayOutputStream bytes;
    private RecyclerView mRecyclerView;
    public static BluetoothSocket mSocket;
    private static InputStream inputStream;
    private static OutputStream outputStream;
    private BlueDeviceItemAdapter mRecycler;
    private Button re_scan;
    private ProgressDialog pd;
    public static int item_locale;
    public static boolean connect_ok;

    @SuppressLint("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);
        re_scan = findViewById(R.id.re_scan);
        re_scan.setOnClickListener(v -> {
            goAnim(this,50);
            disconnectFromDevice();
            searchBluetooth();
        });
        MainActivity.ScanING=true;
        displayList();
        re_scan.setTextSize(16);
        re_scan.setText("重新扫描");
    }

    private void displayList() {
        mRecyclerView = findViewById(R.id.rv_device_list);//设置固定大小
        mRecyclerView.setHasFixedSize(true);//创建线性布局
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.addItemDecoration(new LinearSpacingItemDecoration(10));//添加间距
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)); //添加分隔线
        mRecyclerView.setLayoutManager(layoutManager);
        mRecycler = new BlueDeviceItemAdapter(mlist, this);
        mRecyclerView.setAdapter(mRecycler);
        mRecycler.setRecyclerItemClickListener(position -> {
            goAnim(this,50);
            item_locale = position;
            BleClientActivity.this.showPopupMenu(mRecyclerView.getChildAt(position));
        });
        mRecycler.setRecyclerItemLongClickListener(position -> {
            if (isPaired(mlist.get(item_locale))) {
                goAnim(this,50);
                item_locale = position;
                new AlertDialog.Builder(BleClientActivity.this)
                        .setTitle("取消配对")
                        .setMessage("确定吗?")
                        .setPositiveButton("取消", null)
                        .setNegativeButton("确定", (dialog, which) -> {
                            unpairDevice(mlist.get(item_locale));
                            searchBluetooth();
                        }).show();
            }
        });
    }

    @SuppressLint({"ObsoleteSdkInt"})
    public void searchBluetooth() {
        if(mlist!=null){mlist.clear();}
        displayList();
        MainActivity.ScanING=false;
        discoveryFinished = false;
        re_scan.setText("正在扫描");
        pd = new ProgressDialog(this);
        pd.setMessage("正在扫描,请稍等......");
        pd.show();
        pd.setCancelable(false);
        new Thread(() -> {
            while (!discoveryFinished){
                try {
                    Thread.sleep(500);
                    Message message = new Message();
                    message.what = 1;
                    myHandler.sendMessage(message);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Message message = new Message();
            message.what = 2;
            myHandler.sendMessage(message);
        }).start();
    }
    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                mRecycler = new BlueDeviceItemAdapter(mlist, BleClientActivity.this);
                mRecyclerView.setAdapter(mRecycler);
            }
            if (msg.what == 2) {stopDiscovery();}
        }
    };
    private void stopDiscovery() {
        displayList();
        pd.dismiss();
        re_scan.setTextSize(16);
        re_scan.setText("重新扫描");
    }
    @SuppressLint("MissingPermission")
    private void showPopupMenu(final View view) {
        final PopupMenu popupMenu = new PopupMenu(this, view, Gravity.END);
        //menu 布局
        popupMenu.getMenuInflater().inflate(R.menu.connectmenu, popupMenu.getMenu());
        //点击事件
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.connect_item) {//连接蓝牙
                goAnim(this,50);
                if (connect_ok) {
                    Toast.makeText(this, "巳连接请勿重复连接!", Toast.LENGTH_SHORT).show();
                }else {
                    connectToDevice(mlist.get(item_locale));
                }
            } else if (itemId == R.id.disconnect_item) {//断开连接
                goAnim(this,50);
                disconnectFromDevice();
                connect_ok=false;
                hc06_online=false;
                deleteData("blueDeviceName");
                deleteData("blueDeviceAddress");
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
            //noinspection rawtypes
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            showLog("unpairDevice:"+e.getMessage());
        }
    }
    // 连接到设备
    @SuppressLint("MissingPermission")
    public static void connectToDevice(BluetoothDevice device) {
        new Thread(() -> {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            try {
                mSocket = device.createRfcommSocketToServiceRecord(uuid);
                mSocket.connect();
                // 获取输入输出流
                inputStream = mSocket.getInputStream();
                outputStream = mSocket.getOutputStream();
            } catch (IOException e) {
                // 处理连接异常
            }
        }).start();
    }

    // 发送数据
    public static void sendData(String data) {
        try {
            if (!data.isEmpty()) {
                outputStream.write(data.getBytes());
            }
        } catch (IOException e) {
            // 处理发送异常
        }
    }
    // 接收数据
    public static void receiveData() {
        new Thread(() -> {
        bytes = new ByteArrayOutputStream();
        int len;
        try {
            while ((len = inputStream.read()) != '\n') {
                bytes.write(len); // 写入到缓冲区
            }
            inputData = bytes.toString("UTF-8");
            debugData = inputData;
            Log.d(TAG, "receiveData:" + inputData);
            }catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 断开蓝牙连接
    public static void disconnectFromDevice() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                // 处理关闭异常
            }
        }
    }
    private static void showLog(String text){
        Log.d(TAG, "showLog: " + text);
    }
    protected void onDestroy() {
        super.onDestroy();
    }
    @SuppressLint("MissingPermission")
    protected void onResume() {
        super.onResume();
    }
}
