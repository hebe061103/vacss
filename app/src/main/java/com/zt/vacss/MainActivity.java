package com.zt.vacss;

import static android.widget.Toast.LENGTH_SHORT;
import static com.zt.vacss.BleClientActivity.connect_ok;
import static com.zt.vacss.BleClientActivity.disconnectFromDevice;
import static com.zt.vacss.BleClientActivity.inputData;
import static com.zt.vacss.BleClientActivity.item_locale;
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
import android.os.Looper;
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

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/** @noinspection deprecation*/
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    public String TAG = "MainActivity";
    private static final int REQUEST_CODE_BLUETOOTH_PERMISSIONS = 123;
    private static final String[] BLUETOOTH_PERMISSIONS = {
            Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static List<BluetoothDevice> mlist = new ArrayList<>();
    public static SharedPreferences sp;
    public static SharedPreferences.Editor editor;
    public static String[] parts;
    public ImageView menu_bt;
    long lastBack = 0;
    @SuppressLint("StaticFieldLeak")
    public static TextView bl_data, left_move_speed, left_move_time, right_move_speed, right_move_time, left_move_speed_low, low_left_move_time, right_move_speed_low,
            low_right_move_time, left_Rotation_angle, left_Rotation_angle_still_time, right_Rotation_angle, right_Rotation_angle_still_time, sga_left_Rotation_angle,
            sga_left_Rotation_angle_still_time, sga_right_Rotation_angle, sga_right_Rotation_angle_still_time, sgb_left_Rotation_angle, sgb_left_Rotation_angle_still_time,
            sgb_right_Rotation_angle, sgb_right_Rotation_angle_still_time;
    @SuppressLint("StaticFieldLeak")
    public static Button mEA, left_speed_del, left_speed_add, left_time_del, left_time_add, right_move_speed_del, right_move_speed_add, right_move_time_del, right_move_time_add,
            left_speed_del_low, left_speed_add_low, low_left_time_del, low_left_time_add, right_move_speed_del_low, right_move_speed_add_low, low_right_time_del, low_right_time_add,
            left_Rotation_angle_del, left_Rotation_angle_add, left_Rotation_angle_still_time_del, left_Rotation_angle_still_time_add, right_Rotation_angle_del, right_Rotation_angle_add,
            right_Rotation_angle_still_time_del, right_Rotation_angle_still_time_add, sga_left_Rotation_angle_del, sga_left_Rotation_angle_add, sga_left_Rotation_angle_still_time_del,
            sga_left_Rotation_angle_still_time_add, sga_right_Rotation_angle_del, sga_right_Rotation_angle_add, sga_right_Rotation_angle_still_time_del, sga_right_Rotation_angle_still_time_add,
            sgb_left_Rotation_angle_del, sgb_left_Rotation_angle_add, sgb_left_Rotation_angle_still_time_del, sgb_left_Rotation_angle_still_time_add, sgb_right_Rotation_angle_del, sgb_right_Rotation_angle_add,
            sgb_right_Rotation_angle_still_time_del, sgb_right_Rotation_angle_still_time_add;
    public static volatile boolean discoveryFinished, ScanING, hc06_online;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "本设备没有蓝牙功能", Toast.LENGTH_SHORT).show();
            finish();
        }
        requestBluetoothPermissions();
    }

    private void init() {
        SharedPreferences sp = getSharedPreferences("BlueInfo", MODE_PRIVATE);//获取 SharedPreferences对象
        editor = sp.edit(); // 获取编辑器对象
        menu_bt = findViewById(R.id.menu_img);
        bl_data = findViewById(R.id.bl_connect_status);
        mEA = findViewById(R.id.EA);
        left_move_speed = findViewById(R.id.left_move_speed);
        left_speed_del = findViewById(R.id.left_speed_del);
        left_speed_add = findViewById(R.id.left_speed_add);
        left_move_time = findViewById(R.id.left_move_time);
        left_time_del = findViewById(R.id.left_time_del);
        left_time_add = findViewById(R.id.left_time_add);
        right_move_speed = findViewById(R.id.right_move_speed);
        right_move_speed_del = findViewById(R.id.right_move_speed_del);
        right_move_speed_add = findViewById(R.id.right_move_speed_add);
        right_move_time = findViewById(R.id.right_move_time);
        right_move_time_del = findViewById(R.id.right_move_time_del);
        right_move_time_add = findViewById(R.id.right_move_time_add);
        left_move_speed_low = findViewById(R.id.left_move_speed_low);
        left_speed_del_low = findViewById(R.id.left_speed_del_low);
        left_speed_add_low = findViewById(R.id.left_speed_add_low);
        low_left_move_time = findViewById(R.id.low_left_move_time);
        low_left_time_del = findViewById(R.id.low_left_time_del);
        low_left_time_add = findViewById(R.id.low_left_time_add);
        right_move_speed_low = findViewById(R.id.right_move_speed_low);
        right_move_speed_del_low = findViewById(R.id.right_move_speed_del_low);
        right_move_speed_add_low = findViewById(R.id.right_move_speed_add_low);
        low_right_move_time = findViewById(R.id.low_right_move_time);
        low_right_time_del = findViewById(R.id.low_right_time_del);
        low_right_time_add = findViewById(R.id.low_right_time_add);
        left_Rotation_angle = findViewById(R.id.left_Rotation_angle);
        left_Rotation_angle_del = findViewById(R.id.left_Rotation_angle_del);
        left_Rotation_angle_add = findViewById(R.id.left_Rotation_angle_add);
        left_Rotation_angle_still_time = findViewById(R.id.left_Rotation_angle_still_time);
        left_Rotation_angle_still_time_del = findViewById(R.id.left_Rotation_angle_still_time_del);
        left_Rotation_angle_still_time_add = findViewById(R.id.left_Rotation_angle_still_time_add);
        right_Rotation_angle = findViewById(R.id.right_Rotation_angle);
        right_Rotation_angle_del = findViewById(R.id.right_Rotation_angle_del);
        right_Rotation_angle_add = findViewById(R.id.right_Rotation_angle_add);
        right_Rotation_angle_still_time = findViewById(R.id.right_Rotation_angle_still_time);
        right_Rotation_angle_still_time_del = findViewById(R.id.right_Rotation_angle_still_time_del);
        right_Rotation_angle_still_time_add = findViewById(R.id.right_Rotation_angle_still_time_add);
        sga_left_Rotation_angle = findViewById(R.id.sga_left_Rotation_angle);
        sga_left_Rotation_angle_del = findViewById(R.id.sga_left_Rotation_angle_del);
        sga_left_Rotation_angle_add = findViewById(R.id.sga_left_Rotation_angle_add);
        sga_left_Rotation_angle_still_time = findViewById(R.id.sga_left_Rotation_angle_still_time);
        sga_left_Rotation_angle_still_time_del = findViewById(R.id.sga_left_Rotation_angle_still_time_del);
        sga_left_Rotation_angle_still_time_add = findViewById(R.id.sga_left_Rotation_angle_still_time_add);
        sga_right_Rotation_angle = findViewById(R.id.sga_right_Rotation_angle);
        sga_right_Rotation_angle_del = findViewById(R.id.sga_right_Rotation_angle_del);
        sga_right_Rotation_angle_add = findViewById(R.id.sga_right_Rotation_angle_add);
        sga_right_Rotation_angle_still_time = findViewById(R.id.sga_right_Rotation_angle_still_time);
        sga_right_Rotation_angle_still_time_del = findViewById(R.id.sga_right_Rotation_angle_still_time_del);
        sga_right_Rotation_angle_still_time_add = findViewById(R.id.sga_right_Rotation_angle_still_time_add);
        sgb_left_Rotation_angle = findViewById(R.id.sgb_left_Rotation_angle);
        sgb_left_Rotation_angle_del = findViewById(R.id.sgb_left_Rotation_angle_del);
        sgb_left_Rotation_angle_add = findViewById(R.id.sgb_left_Rotation_angle_add);
        sgb_left_Rotation_angle_still_time = findViewById(R.id.sgb_left_Rotation_angle_still_time);
        sgb_left_Rotation_angle_still_time_del = findViewById(R.id.sgb_left_Rotation_angle_still_time_del);
        sgb_left_Rotation_angle_still_time_add = findViewById(R.id.sgb_left_Rotation_angle_still_time_add);
        sgb_right_Rotation_angle = findViewById(R.id.sgb_right_Rotation_angle);
        sgb_right_Rotation_angle_del = findViewById(R.id.sgb_right_Rotation_angle_del);
        sgb_right_Rotation_angle_add = findViewById(R.id.sgb_right_Rotation_angle_add);
        sgb_right_Rotation_angle_still_time = findViewById(R.id.sgb_right_Rotation_angle_still_time);
        sgb_right_Rotation_angle_still_time_del = findViewById(R.id.sgb_right_Rotation_angle_still_time_del);
        sgb_right_Rotation_angle_still_time_add = findViewById(R.id.sgb_right_Rotation_angle_still_time_add);
        buttonListener buttonListener = new buttonListener(this);
        mEA.setOnClickListener(buttonListener);
        left_speed_del.setOnClickListener(buttonListener);
        left_speed_add.setOnClickListener(buttonListener);
        left_time_del.setOnClickListener(buttonListener);
        left_time_add.setOnClickListener(buttonListener);
        right_move_speed_del.setOnClickListener(buttonListener);
        right_move_speed_add.setOnClickListener(buttonListener);
        right_move_time_del.setOnClickListener(buttonListener);
        right_move_time_add.setOnClickListener(buttonListener);
        left_speed_del_low.setOnClickListener(buttonListener);
        left_speed_add_low.setOnClickListener(buttonListener);
        low_left_time_del.setOnClickListener(buttonListener);
        low_left_time_add.setOnClickListener(buttonListener);
        right_move_speed_del_low.setOnClickListener(buttonListener);
        right_move_speed_add_low.setOnClickListener(buttonListener);
        low_right_time_del.setOnClickListener(buttonListener);
        low_right_time_add.setOnClickListener(buttonListener);
        left_Rotation_angle_del.setOnClickListener(buttonListener);
        left_Rotation_angle_add.setOnClickListener(buttonListener);
        left_Rotation_angle_still_time_del.setOnClickListener(buttonListener);
        left_Rotation_angle_still_time_add.setOnClickListener(buttonListener);
        right_Rotation_angle_del.setOnClickListener(buttonListener);
        right_Rotation_angle_add.setOnClickListener(buttonListener);
        right_Rotation_angle_still_time_del.setOnClickListener(buttonListener);
        right_Rotation_angle_still_time_add.setOnClickListener(buttonListener);
        sga_left_Rotation_angle_del.setOnClickListener(buttonListener);
        sga_left_Rotation_angle_add.setOnClickListener(buttonListener);
        sga_left_Rotation_angle_still_time_del.setOnClickListener(buttonListener);
        sga_left_Rotation_angle_still_time_add.setOnClickListener(buttonListener);
        sga_right_Rotation_angle_del.setOnClickListener(buttonListener);
        sga_right_Rotation_angle_add.setOnClickListener(buttonListener);
        sga_right_Rotation_angle_still_time_del.setOnClickListener(buttonListener);
        sga_right_Rotation_angle_still_time_add.setOnClickListener(buttonListener);
        sgb_left_Rotation_angle_del.setOnClickListener(buttonListener);
        sgb_left_Rotation_angle_add.setOnClickListener(buttonListener);
        sgb_left_Rotation_angle_still_time_del.setOnClickListener(buttonListener);
        sgb_left_Rotation_angle_still_time_add.setOnClickListener(buttonListener);
        sgb_right_Rotation_angle_del.setOnClickListener(buttonListener);
        sgb_right_Rotation_angle_add.setOnClickListener(buttonListener);
        sgb_right_Rotation_angle_still_time_del.setOnClickListener(buttonListener);
        sgb_right_Rotation_angle_still_time_add.setOnClickListener(buttonListener);
        //设置过滤器，过滤因远程蓝牙设备被找到而发送的广播 BluetoothDevice.ACTION_FOUND
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(BluetoothDevice.ACTION_FOUND);
        iFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        iFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        iFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(foundReceiver, iFilter);
        //设置广播接收器和安装过滤器
        get_event();
    }

    private void buttonListen() {
        menu_bt.setOnClickListener(view -> {
            goAnim(this, 50);
            MainActivity.this.showPopupMenu(menu_bt);
        });
        bl_data.setOnLongClickListener(view -> {
            goAnim(this, 50);
            if (readDate() != null) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("是否要断开连接?")
                        .setMessage("确定吗?")
                        .setPositiveButton("取消", null)
                        .setNegativeButton("确定", (dialog, which) -> {
                            deleteData("blueDeviceName");
                            deleteData("blueDeviceAddress");
                            disconnectFromDevice();
                            connect_ok = false;
                            hc06_online = false;
                            bl_Status();
                        })
                        .show();
            }
            return true;
        });
    }

    /**
     * 当找到一个远程蓝牙设备时执行的广播接收者
     *
     */
    public final BroadcastReceiver foundReceiver = new BroadcastReceiver() {
        @SuppressLint({"MissingPermission", "SetTextI18n", "UnsafeIntentLaunch"})
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//获取此时找到的远程设备对象
            if (device != null && device.getName() != null) {
                Log.d(TAG, "name:" + device.getName() + "\n" + device.getAddress());
                if (!mlist.contains(device)) {
                    mlist.add(device);
                }
            }
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction())) {
                Log.d(TAG, "连接成功");
                Toast.makeText(context, "连接成功", LENGTH_SHORT).show();
                saveData("blueDeviceName", mlist.get(item_locale).getName());
                saveData("blueDeviceAddress", mlist.get(item_locale).getAddress());
                Intent intentService = new Intent(MainActivity.this, MyService.class);
                startService(intentService);
                connect_ok = true;
            }
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {
                Log.d(TAG, "连接断开");
                Toast.makeText(context, "连接断开", LENGTH_SHORT).show();
                connect_ok = false;
                hc06_online = false;
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                Log.d(TAG, "扫描完成");
                //Toast.makeText(context, "扫描完成", Toast.LENGTH_SHORT).show();
                discoveryFinished = true;
            }
        }
    };

    @SuppressLint("MissingPermission")
    public void showPopupMenu(final View view) {
        final PopupMenu popupMenu = new PopupMenu(this, view);
        //menu 布局
        popupMenu.getMenuInflater().inflate(R.menu.main, popupMenu.getMenu());
        //点击事件
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.scan_bt) {
                goAnim(this, 50);
                Intent intent = new Intent(MainActivity.this, BleClientActivity.class);
                startActivities(new Intent[]{intent});
            } else if (itemId == R.id.bt_save) {
                if (hc06_online) {
                    goAnim(this, 50);
                    sendData("/");
                    receiveData();
                } else Toast.makeText(this, "请先连接到设备!", Toast.LENGTH_LONG).show();
            } else if (itemId == R.id.about) {
                goAnim(this, 50);
                Intent intent = new Intent(MainActivity.this, about.class);
                startActivities(new Intent[]{intent});
            }
            return false;
        });
        //显示菜单，不要少了这一步
        popupMenu.show();
    }

    private String readDate() {
        sp = getSharedPreferences("BlueInfo", MODE_PRIVATE);
        return sp.getString("blueDeviceName", null);
    }

    @SuppressLint("MissingPermission")
    private void bl_Status() {
        if (EasyPermissions.hasPermissions(this, BLUETOOTH_PERMISSIONS)) {
            if (connect_ok) {
                bl_data.setTextColor(Color.parseColor("#00ff66"));
                bl_data.setText(readDate());
                if (readDate() != null && readDate().equals("HC-06-115200-N-1")) {
                    hc06_online = true;
                    mEA.setTextColor(Color.parseColor("#00ff66"));
                    sendData("a");//向单片机发送代码
                    //接收单片机返回数据
                    receiveData();
                }
            } else {
                ScanING = false;
                bl_data.setTextColor(Color.parseColor("#CCCCCC"));
                bl_data.setText("未连接");
                mEA.setTextColor(Color.parseColor("#CCCCCC"));
                mEA.setText("启 用");
            }
        }
    }

    public void get_event() {
        new Thread(() -> {
            while (true) {
                while (hc06_online) {
                    if (inputData != null && inputData.contains("save")) {
                        Log.d(TAG, "inputData包含: save");
                        Message message = new Message();
                        message.what = 1;
                        myHandler.sendMessage(message);
                        inputData=null;
                    }
                    if (inputData != null && inputData.contains("begin")) {
                        Log.d(TAG, "inputData包含: begin");
                        Message message = new Message();
                        message.what = 2;
                        myHandler.sendMessage(message);
                        inputData=null;
                    }
                    if (inputData != null && inputData.contains("stop")) {
                        Log.d(TAG, "inputData包含: stop");
                        Message message = new Message();
                        message.what = 3;
                        myHandler.sendMessage(message);
                        inputData=null;
                    }
                    if (inputData != null && inputData.contains("l_pwm")) {
                        parts = inputData.split(",");
                        if (parts.length == 8 && parts[0].equals("l_pwm")) {
                            Log.d(TAG, "inputData包含: l_pwm");
                            Message message = new Message();
                            message.what = 4;
                            myHandler.sendMessage(message);
                            inputData=null;
                        }
                    }
                    if (inputData != null && inputData.contains("lo_pwm")) {
                        parts = inputData.split(",");
                        if (parts.length == 8 && parts[0].equals("lo_pwm")) {
                            Log.d(TAG, "inputData包含: lo_pwm");
                            Message message = new Message();
                            message.what = 5;
                            myHandler.sendMessage(message);
                            inputData=null;
                        }
                    }
                    if (inputData != null && inputData.contains("sga_left")) {
                        parts = inputData.split(",");
                        if (parts.length == 12 && parts[0].equals("sga_left")) {
                            Log.d(TAG, "inputData包含: sga_left");
                            Message message = new Message();
                            message.what = 6;
                            myHandler.sendMessage(message);
                            inputData=null;
                        }
                    }
                    if (inputData != null && inputData.contains("sg90a_time_left")) {
                        parts = inputData.split(",");
                        if (parts.length == 12 && parts[0].equals("sg90a_time_left")) {
                            Log.d(TAG, "inputData包含: sg90a_time_left");
                            Message message = new Message();
                            message.what = 7;
                            myHandler.sendMessage(message);
                            inputData=null;
                        }
                    }
                    if (inputData != null && inputData.contains("ALLDATA")){
                        parts = inputData.split(",");
                        if (parts.length == 43 && parts[0].equals("ALLDATA")) {
                            Log.d(TAG, "inputData包含: ALLDATA");
                            Message message = new Message();
                            message.what = 8;
                            myHandler.sendMessage(message);
                            inputData = null;
                        }
                    }
                }
            }

        }).start();
    }
    public Handler myHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 1) {
                Log.d(TAG, "myHandler进入了: 1");
                Toast.makeText(MainActivity.this, "保存成功", LENGTH_SHORT).show();
            }
            if (msg.what == 2) {
                Log.d(TAG, "myHandler进入了: 2");
                mEA.setText("停 用");
            }
            if (msg.what == 3) {
                Log.d(TAG, "myHandler进入了: 3");
                mEA.setText("启 用");
            }
            if (msg.what == 4) {
                Log.d(TAG, "myHandler进入了: 4");
                left_move_speed.setText(parts[1]);
                left_move_time.setText(parts[5]);
                right_move_speed.setText(parts[3]);
                right_move_time.setText(parts[7]);
            }
            if (msg.what == 5) {
                Log.d(TAG, "myHandler进入了: 5");
                left_move_speed_low.setText(parts[1]);
                low_left_move_time.setText(parts[5]);
                right_move_speed_low.setText(parts[3]);
                low_right_move_time.setText(parts[7]);
            }
            if (msg.what == 6) {
                Log.d(TAG, "myHandler进入了: 6");
                sga_left_Rotation_angle.setText(parts[1]);
                sga_right_Rotation_angle.setText(parts[3]);
                sgb_left_Rotation_angle.setText(parts[5]);
                sgb_right_Rotation_angle.setText(parts[7]);
                left_Rotation_angle.setText(parts[9]);
                right_Rotation_angle.setText(parts[11]);
            }
            if (msg.what == 7) {
                Log.d(TAG, "myHandler进入了: 7");
                sga_left_Rotation_angle_still_time.setText(parts[1]);
                sga_right_Rotation_angle_still_time.setText(parts[3]);
                sgb_left_Rotation_angle_still_time.setText(parts[5]);
                sgb_right_Rotation_angle_still_time.setText(parts[7]);
                left_Rotation_angle_still_time.setText(parts[9]);
                right_Rotation_angle_still_time.setText(parts[11]);
            }
            if (msg.what == 8){
                Log.d(TAG, "myHandler进入了: 8");
                if (parts[2].contains("1")){mEA.setText("停 用");}
                if (parts[2].contains("0")){mEA.setText("启 用");}
                left_move_speed.setText(parts[4]);
                left_move_time.setText(parts[8]);
                right_move_speed.setText(parts[6]);
                right_move_time.setText(parts[10]);
                left_move_speed_low.setText(parts[12]);
                low_left_move_time.setText(parts[16]);
                right_move_speed_low.setText(parts[14]);
                low_right_move_time.setText(parts[18]);
                sga_left_Rotation_angle.setText(parts[20]);
                sga_right_Rotation_angle.setText(parts[22]);
                sgb_left_Rotation_angle.setText(parts[24]);
                sgb_right_Rotation_angle.setText(parts[26]);
                left_Rotation_angle.setText(parts[28]);
                right_Rotation_angle.setText(parts[30]);
                sga_left_Rotation_angle_still_time.setText(parts[32]);
                sga_right_Rotation_angle_still_time.setText(parts[34]);
                sgb_left_Rotation_angle_still_time.setText(parts[36]);
                sgb_right_Rotation_angle_still_time.setText(parts[38]);
                left_Rotation_angle_still_time.setText(parts[40]);
                right_Rotation_angle_still_time.setText(parts[42]);
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void DiscoveryDevice() {
        new Thread(() -> {
            while (true) {
                if (!connect_ok) {
                    if (!ScanING) {
                        mBluetoothAdapter.startDiscovery();
                        ScanING = true;
                        Log.d(TAG, "defaultDevice: 开始搜索设备");
                    }
                }
            }
        }).start();
    }

    public static void saveData(String l, String s) {//l为保存的名字，s为要保存的字符串
        editor.putString(l, s);
        editor.apply();
    }

    public static void deleteData(String l) {
        editor.remove(l); // 根据l删除数据
        editor.apply();
    }

    @SuppressLint("MissingPermission")
    protected void onResume() {
        super.onResume();
        bl_Status();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 简单震动
     * @param context     调用震动的Context
     * @param millisecond 震动的时间，毫秒
     */
    @SuppressWarnings("static-access")
    public static void goAnim(Context context, int millisecond) {
        Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
        vibrator.vibrate(millisecond);
    }

    /**
     * 再次返回键退出程序
     */
    @Override
    public void onBackPressed() {
        if (lastBack == 0 || System.currentTimeMillis() - lastBack > 2000) {
            Toast.makeText(MainActivity.this, "再按一次返回退出", LENGTH_SHORT).show();
            lastBack = System.currentTimeMillis();
            return;
        }
        super.onBackPressed();
    }

    @AfterPermissionGranted(REQUEST_CODE_BLUETOOTH_PERMISSIONS)
    private void requestBluetoothPermissions() {
        if  (EasyPermissions.hasPermissions(this, BLUETOOTH_PERMISSIONS)) {
            init();
            DiscoveryDevice();
            buttonListen();
        } else {
            // 没有获得权限，请求权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    EasyPermissions.requestPermissions(this, "需要蓝牙权限以扫描周围的蓝牙设备",
                            REQUEST_CODE_BLUETOOTH_PERMISSIONS, Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN);
                }
            }
            EasyPermissions.requestPermissions(this, "需要蓝牙权限以扫描周围的蓝牙设备", REQUEST_CODE_BLUETOOTH_PERMISSIONS, BLUETOOTH_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 将结果传递给EasyPermissions处理
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_CODE_BLUETOOTH_PERMISSIONS) {
            // 相关权限被授予，可以进行蓝牙操作
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_CODE_BLUETOOTH_PERMISSIONS) {
            // 权限被拒绝，可以适当处理
            finish();
        }
    }
}
