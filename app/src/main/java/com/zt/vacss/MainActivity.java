package com.zt.vacss;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
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

import pub.devrel.easypermissions.EasyPermissions;

/** @noinspection deprecation*/
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    TextView serverStatus_remind;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startEnableBluetooth();
        initClickListen();
        serverStatusDisplay();
    }

    private void initClickListen() {
        serverStatus_remind = findViewById(R.id.server_status_flag);
        ImageView menu_bt = findViewById(R.id.menu_img);
        menu_bt.setOnClickListener(view -> {
            goAnim();
            MainActivity.this.showPopupMenu(menu_bt);
        });
        serverStatus_remind.setOnLongClickListener(view -> {
            goAnim();
            if (!serverStatus_remind.getText().toString().equals("服务停止")) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("取消持续信号检测吗?")
                        .setMessage("确定吗?")
                        .setPositiveButton("取消", null)
                        .setNegativeButton("确定", (dialog, which) -> {
                            Intent intent = new Intent(MainActivity.this, RefreshRssi.class);
                            stopService(intent);
                        })
                        .show();
            } else {
                Toast.makeText(MainActivity.this, "服务没有运行", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
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
    private void serverStatusDisplay(){
        if(RefreshRssi.serverFlag){
            serverStatus_remind.setTextColor(Color.parseColor("#33cc00"));
            serverStatus_remind.setText("服务运行中");
        }else {
            serverStatus_remind.setTextColor(Color.parseColor("#cccccc"));
            serverStatus_remind.setText("服务停止");
        }
    }
    protected void onResume() {
        startEnableBluetooth();
        serverStatusDisplay();
        super.onResume();
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
