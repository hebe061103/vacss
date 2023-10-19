package com.zt.vacss;


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
import android.graphics.drawable.ClipDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.EasyPermissions;
/** @noinspection deprecation*/
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ClipDrawable clipBackground;
    private ImageView mImageView;
    private TextView rssi_value;
    private Boolean exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startEnableBluetooth();
        init();
    }
    private void init() {
        mImageView = findViewById(R.id.iv);
        clipBackground = (ClipDrawable) mImageView.getDrawable();
        //设置过滤器，过滤因远程蓝牙设备被找到而发送的广播 BluetoothDevice.ACTION_FOUND
        IntentFilter iFilter=new IntentFilter();
        iFilter.addAction(BluetoothDevice.ACTION_FOUND);
        //设置广播接收器和安装过滤器
        registerReceiver(new foundReceiver(), iFilter);
        ImageView menu_bt = findViewById(R.id.menu_img);
        rssi_value = findViewById(R.id.display_value);
        menu_bt.setOnClickListener(view -> {
            goAnim();
            MainActivity.this.showPopupMenu(menu_bt);
        });
        rssi_value.setOnLongClickListener(view -> {
            if (!(readDate() == null)) {
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
        final Handler handler = new Handler(msg -> {
            if(msg.what == 0x123456){
                clipBackground.setLevel(clipBackground.getLevel()+800);
            }
            return true;
        });
        //定时器，第一次启动的时间是0，每隔300ms执行一次，当clipDrawable.getLevel()大于10000的时候，取消定时器
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 0x123456;
                handler.sendMessage(msg);
                if(clipBackground.getLevel()>10000){
                    //timer.cancel();
                    clipBackground.setLevel(0);
                }
            }
        },0,100);
    }

    private void showPopupMenu(final View view) {
        final PopupMenu popupMenu = new PopupMenu(this, view);
        //menu 布局
        popupMenu.getMenuInflater().inflate(R.menu.main, popupMenu.getMenu());
        //点击事件
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.scan_bt) {
                goAnim();
                exit=true;
                Intent intent = new Intent(MainActivity.this, BleClientActivity.class);
                startActivities(new Intent[]{intent});
            } else if (itemId == R.id.about) {
                goAnim();
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
            showToast("本设备没有蓝牙功能");
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
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//获取此时找到的远程设备对象
            assert device != null;
            if (readDate().equals(device.getAddress())) {//判断远程设备是否与用户目标设备相同
                short rssi = Objects.requireNonNull(intent.getExtras()).getShort(BluetoothDevice.EXTRA_RSSI);//获取额外rssi值
                rssi_value.setText(device.getName() + "\n"+"信号值:" + rssi);//显示rssi到控件上
                mBluetoothAdapter.cancelDiscovery();//关闭搜索
            } else {
                rssi_value.setText("❀");
            }
        }
    }
    private String readDate(){
        SharedPreferences sp = getSharedPreferences("data",MODE_PRIVATE);
        return sp.getString("deviceName","");
    }
    private void deleteData(String name){
        SharedPreferences sp = getSharedPreferences("data",MODE_PRIVATE);//获取 SharedPreferences对象
        SharedPreferences.Editor editor = sp.edit(); // 获取编辑器对象
        editor.remove(name); // 根据key删除数据
        editor.apply();
    }
    private void scanRssiValue(){
        new Thread(){
            /** @noinspection InfiniteLoopStatement*/
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                    while (!exit) {
                        mBluetoothAdapter.startDiscovery();
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        }.start();
    }
    protected void onResume() {
        startEnableBluetooth();
        if(!(readDate()==null)){exit=false; scanRssiValue();}
        readDate();
        super.onResume();
    }
    protected void onDestroy() {
        super.onDestroy();
    }
    protected void goAnim(){
        // 震动效果的系统服务
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(20);//振动0.5秒
        // 下边是可以使震动有规律的震动  -1：表示不重复 0：循环的震动
    }
    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
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
