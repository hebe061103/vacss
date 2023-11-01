package com.zt.vacss;


import static com.zt.vacss.BleClientActivity.connectToDevice;
import static com.zt.vacss.BleClientActivity.connect_ok;
import static com.zt.vacss.BleClientActivity.disconnectFromDevice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;
/** @noinspection deprecation*/
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    static BluetoothDevice device;
    private TextView rssi_value,bl_data;
    private Boolean exit=false;
    long lastBack = 0;
    private String dl_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        defaultDevice();
    }
    private void init() {
        CircularProgressView progress_bar = findViewById(R.id.progress_bar);
        CircularProgressView progress_bar2 = findViewById(R.id.progress_bar2);
        //设置过滤器，过滤因远程蓝牙设备被找到而发送的广播 BluetoothDevice.ACTION_FOUND
        IntentFilter iFilter=new IntentFilter();
        iFilter.addAction(BluetoothDevice.ACTION_FOUND);
        iFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        iFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(new foundReceiver(), iFilter);
        //设置广播接收器和安装过滤器
        ImageView menu_bt = findViewById(R.id.menu_img);
        bl_data = findViewById(R.id.bl_connect_status);
        rssi_value = findViewById(R.id.display_value);
        menu_bt.setOnClickListener(view -> {
            goAnim();
            MainActivity.this.showPopupMenu(menu_bt);
        });
        rssi_value.setOnLongClickListener(view -> {
            if (!(readDate("deviceName") == null)) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("取消信号检测吗?")
                        .setMessage("确定吗?")
                        .setPositiveButton("取消", null)
                        .setNegativeButton("确定", (dialog, which) -> {
                            deleteData("deviceName");
                            rssi_value.setText("❀");
                            exit = true;
                        })
                        .show();
            }
            return true;
        });
        bl_data.setOnLongClickListener(view -> {
                if (readDate("online")!=null) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("是否要删除默认连接?")
                            .setMessage("确定吗?")
                            .setPositiveButton("取消", null)
                            .setNegativeButton("确定", (dialog, which) -> {
                                disconnectFromDevice();
                                connect_ok=false;
                                deleteData("online");
                                bl_Status();
                            })
                            .show();
                }
            return true;
        });
        progress_bar.setProgress(50);
        progress_bar.setText(50+"%");
        progress_bar2.setProgress(20);
        progress_bar2.setText(20+"%");

    }

    @SuppressLint("MissingPermission")
    private void showPopupMenu(final View view) {
        final PopupMenu popupMenu = new PopupMenu(this, view);
        //menu 布局
        popupMenu.getMenuInflater().inflate(R.menu.main, popupMenu.getMenu());
        //点击事件
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.scan_bt) {
                exit=true;
                goAnim();
                mBluetoothAdapter.cancelDiscovery();
                Intent intent = new Intent(MainActivity.this, BleClientActivity.class);
                startActivities(new Intent[]{intent});
            } else if (itemId == R.id.about) {
                exit=true;
                goAnim();
                mBluetoothAdapter.cancelDiscovery();
                Intent intent = new Intent(MainActivity.this, about.class);
                startActivities(new Intent[]{intent});
            }
            return false;
        });
        //显示菜单，不要少了这一步
        popupMenu.show();
    }
    private void startEnableBluetooth() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "本设备没有蓝牙功能", Toast.LENGTH_SHORT).show();
            return;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT
                }, 200);
            }
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.BLUETOOTH_SCAN},100);
            }
        }
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 200);
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 200);
        }
    }
    /**
     * 内部类：当找到一个远程蓝牙设备时执行的广播接收者
     * @author Administrator
     *
     */
    class foundReceiver extends BroadcastReceiver {
        @SuppressLint({"MissingPermission", "SetTextI18n"})
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//获取此时找到的远程设备对象
            assert device != null;
            if (readDate("deviceName")!=null && readDate("deviceName").equals(device.getAddress())) {//判断远程设备是否与用户目标设备相同
                short rssi = Objects.requireNonNull(intent.getExtras()).getShort(BluetoothDevice.EXTRA_RSSI);//获取额外rssi值
                rssi_value.setText(device.getName() + "\n"+"信号值:" + rssi);//显示rssi到控件上
                mBluetoothAdapter.cancelDiscovery();//关闭搜索
                exit=false;
            }else {
                rssi_value.setText("❀");
            }
            if (readDate("online")!=null && readDate("online").equals(device.getAddress())) {//判断远程设备是否与用户目标设备相同
                connectToDevice(device);
            }
            if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction())){
                connect_ok=true;
                dl_name=device.getName();
                bl_Status();
            }
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())){
                connect_ok=false;
                bl_Status();
            }
        }
    }
    private String readDate(String key){
        SharedPreferences sp = getSharedPreferences("data",MODE_PRIVATE);
        return sp.getString(key,null);
    }
    private void deleteData(String key){
        SharedPreferences sp = getSharedPreferences("data",MODE_PRIVATE);//获取 SharedPreferences对象
        SharedPreferences.Editor editor = sp.edit(); // 获取编辑器对象
        editor.remove(key); // 根据key删除数据
        editor.apply();
    }
    @SuppressLint("MissingPermission")
    private void scanRssiValue(){
        new Thread(() -> {
            while (!exit) {
                exit=true;
                mBluetoothAdapter.startDiscovery();
                sleepMS();
            }
        }).start();
    }
    @SuppressLint("MissingPermission")
    private void bl_Status(){
        if(connect_ok) {
            bl_data.setTextColor(Color.parseColor("#00ff66"));
            bl_data.setTextSize(18);
            bl_data.setText(dl_name);
        }else {
            bl_data.setTextColor(Color.parseColor("#CCCCCC"));
            bl_data.setTextSize(18);
            bl_data.setText("未连接");
        }
    }
    @SuppressLint("MissingPermission")
    private void defaultDevice(){
        new Thread(() -> {
            while(true){
                if(readDate("online")!=null && !connect_ok){
                    mBluetoothAdapter.startDiscovery();
                }
            }
        }).start();
    }
    private void sleepMS(){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    protected void onResume() {
        startEnableBluetooth();
        if(!(readDate("deviceName") == null)){exit=false; scanRssiValue();}
        bl_Status();
        super.onResume();
    }
    protected void onDestroy() {
        super.onDestroy();
    }
    protected void goAnim(){
        // 震动效果的系统服务
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(30);//振动0.5秒
        // 下边是可以使震动有规律的震动  -1：表示不重复 0：循环的震动
    }
    /**
     * 再次返回键退出程序
     */
    @Override
    public void onBackPressed() {
        if (lastBack == 0 || System.currentTimeMillis() - lastBack > 2000) {
            Toast.makeText(MainActivity.this, "再按一次返回退出", Toast.LENGTH_SHORT).show();
            lastBack = System.currentTimeMillis();
            return;
        }
        super.onBackPressed();
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 回调结果传递给EasyPermission
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        finish();
    }
}
