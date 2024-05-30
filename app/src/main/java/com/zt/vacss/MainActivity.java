package com.zt.vacss;

import static com.zt.vacss.BleClientActivity.connect_ok;
import static com.zt.vacss.BleClientActivity.disconnectFromDevice;
import static com.zt.vacss.BleClientActivity.inputData;
import static com.zt.vacss.BleClientActivity.receiveData;
import static com.zt.vacss.BleClientActivity.sendData;

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
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/** @noinspection deprecation*/
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static List<BluetoothDevice> mlist = new ArrayList<>();
    @SuppressLint("StaticFieldLeak")
    public static CircularProgressView progress_bar,progress_bar2;
    public static SharedPreferences sp;
    public static SharedPreferences.Editor editor ;
    public static String[] parts;
    public static int left_pwm,right_pwm;
    @SuppressLint("StaticFieldLeak")
    public static TextView bl_data;
    @SuppressLint("StaticFieldLeak")
    public static Button mEA;
    public ImageView menu_bt;
    public static Message message;
    long lastBack = 0;
    public static volatile boolean discoveryFinished,ScanING,hc06_online;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        defaultDevice();
    }

    private void init() {
        mEA = findViewById(R.id.EA);
        menu_bt = findViewById(R.id.menu_img);
        bl_data = findViewById(R.id.bl_connect_status);
        SharedPreferences sp = getSharedPreferences("BlueInfo",MODE_PRIVATE);//获取 SharedPreferences对象
        editor = sp.edit(); // 获取编辑器对象
        progress_bar = findViewById(R.id.progress_bar);
        progress_bar2 = findViewById(R.id.progress_bar2);
        //设置过滤器，过滤因远程蓝牙设备被找到而发送的广播 BluetoothDevice.ACTION_FOUND
        IntentFilter iFilter=new IntentFilter();
        iFilter.addAction(BluetoothDevice.ACTION_FOUND);
        iFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        iFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        iFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(foundReceiver, iFilter);
        //设置广播接收器和安装过滤器
        mEA.setOnClickListener(view -> {
            if (mEA.getText().toString().equals("启 用")) {
                goAnim();
                sendData("y");
                receiveData();//接收单片机返回的数据
            }
            if (mEA.getText().toString().equals("停 用")) {
                goAnim();
                sendData("z");
                receiveData();//接收单片机返回的数据
            }
        });
        menu_bt.setOnClickListener(view -> {
            goAnim();
            MainActivity.this.showPopupMenu(menu_bt);
        });
        bl_data.setOnLongClickListener(view -> {
            goAnim();
            if (readDate() != null) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("是否要删除默认连接?")
                        .setMessage("确定吗?")
                        .setPositiveButton("取消", null)
                        .setNegativeButton("确定", (dialog, which) -> {
                            deleteData("blueDeviceName");
                            deleteData("blueDeviceAddress");
                            disconnectFromDevice();
                            connect_ok = false;
                            hc06_online=false;
                            bl_Status();
                        })
                        .show();
            }
            return true;
        });
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                while(true){
                    Thread.sleep(500);
                    if (inputData!=null&&inputData.contains("l_pwm")) {parts = inputData.split(",");left_pwm=Integer.parseInt(parts[1]);}
                    if (inputData!=null&&inputData.contains("r_pwm")) {parts = inputData.split(",");right_pwm=Integer.parseInt(parts[1]);}
                    if (inputData != null && inputData.contains("run")) {mEA.setTextColor(Color.parseColor("#99FF66"));message = myHandler.obtainMessage();message.what = 8;myHandler.sendMessage(message);}
                    if (inputData != null && inputData.contains("stop")) {mEA.setTextColor(Color.parseColor("#99FF66"));message = myHandler.obtainMessage();message.what = 9;myHandler.sendMessage(message);}
                    if (hc06_online) {message = myHandler.obtainMessage();message.what = 2;myHandler.sendMessage(message);}else{message = myHandler.obtainMessage();message.what = 3;myHandler.sendMessage(message);}
                    if (left_pwm!=0) {message = myHandler.obtainMessage();message.what = 4;myHandler.sendMessage(message);}else {message = myHandler.obtainMessage();message.what = 5;myHandler.sendMessage(message);}
                    if (right_pwm!=0) {message = myHandler.obtainMessage();message.what = 6;myHandler.sendMessage(message);}else {message = myHandler.obtainMessage();message.what = 7;myHandler.sendMessage(message);}
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what==2){mEA.setTextColor(Color.parseColor("#99FF66"));}
            if (msg.what==3){mEA.setTextColor(Color.parseColor("#CCCCCC"));}
            if (msg.what==4){right_pwm=0;progress_bar.setProgress(left_pwm);progress_bar.setText(left_pwm + "%");progress_bar2.setProgress(0);progress_bar2.setText(0 + "%");}
            if (msg.what==5){progress_bar.setProgress(0);progress_bar.setText(0 + "%");}
            if (msg.what==6){left_pwm=0;progress_bar.setProgress(0);progress_bar.setText(0 + "%");progress_bar2.setProgress(right_pwm);progress_bar2.setText(right_pwm + "%");}
            if (msg.what==7){progress_bar2.setProgress(0);progress_bar2.setText(0 + "%");}
            if (msg.what==8){mEA.setText("停 用");}
            if (msg.what==9){mEA.setText("启 用");}
        }
    };
    /**
     * 当找到一个远程蓝牙设备时执行的广播接收者
     *
     */
    public final BroadcastReceiver foundReceiver = new BroadcastReceiver() {
        @SuppressLint({"MissingPermission", "SetTextI18n", "UnsafeIntentLaunch"})
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//获取此时找到的远程设备对象
            if(device !=null && device.getName()!=null){
                Log.d("找到的设备:", "name:"+device.getName()+"\n"+device.getAddress());
                if (!mlist.contains(device)){mlist.add(device);}
            }
            if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction())){
                Log.d("连接状态:", "连接成功");
                Toast.makeText(context, "连接成功", Toast.LENGTH_SHORT).show();
                connect_ok=true;
            }
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())){
                Log.d("连接状态:", "连接断开");
                Toast.makeText(context, "连接断开", Toast.LENGTH_SHORT).show();
                connect_ok=false;
                hc06_online=false;
            }
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())){
                Log.d("扫描状态:", "扫描完成");
                //Toast.makeText(context, "扫描完成", Toast.LENGTH_SHORT).show();
                discoveryFinished = true;
            }
        }
    };
    @SuppressLint("MissingPermission")
    private void showPopupMenu(final View view) {
        final PopupMenu popupMenu = new PopupMenu(this, view);
        //menu 布局
        popupMenu.getMenuInflater().inflate(R.menu.main, popupMenu.getMenu());
        //点击事件
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.scan_bt) {
                goAnim();
                Intent intent = new Intent(MainActivity.this, BleClientActivity.class);
                startActivities(new Intent[]{intent});
            }else if (itemId == R.id.debug_device) {
                goAnim();
                Intent intent = new Intent(MainActivity.this, DebugDevice.class);
                startActivities(new Intent[]{intent});
            }else if (itemId == R.id.about) {
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
            Toast.makeText(this, "本设备没有蓝牙功能", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT
                }, 200);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 100);
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
    private String readDate(){
        sp = getSharedPreferences("BlueInfo",MODE_PRIVATE);
        return sp.getString("blueDeviceName",null);
    }
    @SuppressLint("MissingPermission")
    private void bl_Status(){
        if(connect_ok) {
            bl_data.setTextColor(Color.parseColor("#00ff66"));
            bl_data.setTextSize(16);
            bl_data.setText(readDate());
            get51Data();
        }else {
            bl_data.setTextColor(Color.parseColor("#CCCCCC"));
            bl_data.setTextSize(18);
            bl_data.setText("未连接");
            mEA.setTextColor(Color.parseColor("#CCCCCC"));
            bl_data.setTextSize(20);
            mEA.setText("启 用");
        }
    }
    public static void get51Data(){
        if (hc06_online) {
            sendData("a");//连接到了指定蓝牙，发送指定代码获取单片机数据
            receiveData();//接收单片机返回的数据
            Log.d("BL_receive", "收到的回复:" + inputData);
        }
    }
    @SuppressLint("MissingPermission")
    private void defaultDevice(){
        new Thread(() -> {
            while (true) {
                if (!connect_ok) {
                    if (!ScanING) {
                        mBluetoothAdapter.startDiscovery();
                        ScanING = true;
                        Log.d("Main", "defaultDevice: 开始搜索设备");
                    }
                }
            }
        }).start();
    }
    public static void saveData(String l ,String s){//l为保存的名字，s为要保存的字符串
        editor.putString(l,s);
        editor.apply();
    }

    public static void  deleteData(String l){
        editor.remove(l); // 根据l删除数据
        editor.apply();
    }
    @SuppressLint("MissingPermission")
    protected void onResume() {
        super.onResume();
        startEnableBluetooth();
        bl_Status();
        ScanING=false;
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
