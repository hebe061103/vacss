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
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/** @noinspection deprecation*/
public class BleClientActivity extends AppCompatActivity implements EasyPermissions.RationaleCallbacks{
    private final List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private static final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private RecyclerView mRecyclerView;
    private Button re_scan;
    private ProgressDialog pd;
    private int item_locale;
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
    }

    public void searchBluetooth() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.BLUETOOTH_SCAN},100);
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
                mDeviceList.add(device);
                BlueDeviceItemAdapter mRecycler = new BlueDeviceItemAdapter(mDeviceList, this);
                mRecyclerView.setAdapter(mRecycler);
                mRecyclerView.addItemDecoration(new LinearSpacingItemDecoration(this, 10));//添加间距
                mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)); //添加分隔线
                mRecycler.setRecyclerItemLongClickListener(position -> {
                    goAnim();
                    item_locale = position;
                    showPopupMenu(mRecyclerView.getChildAt(position));
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
                //BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceList.get(item_locale).getAddress());
            } else if (itemId == R.id.disconnect_item) {
                goAnim();
            }
            return false;
        });
        popupMenu.show();//显示菜单，不要少了这一步
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

    //抖动震动
    protected void goAnim(){
        // 震动效果的系统服务
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(30);//振动0.5秒
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
}
