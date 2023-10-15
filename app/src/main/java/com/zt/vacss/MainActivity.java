package com.zt.vacss;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import pub.devrel.easypermissions.EasyPermissions;

/** @noinspection deprecation*/
public class MainActivity extends AppCompatActivity implements EasyPermissions.RationaleCallbacks{
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startEnableBluetooth();
        ImageView menu_bt = findViewById(R.id.menu_img);
        menu_bt.setOnClickListener(view -> {
            goAnim();
            MainActivity.this.showPopupMenu(menu_bt);
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
            }else if (itemId == R.id.bond_bt) {
                goAnim();
                Intent intent = new Intent(MainActivity.this, bond_bt.class);
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
        if ((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, 300);
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 200);
        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 回调结果传递给EasyPermission
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    protected void goAnim(){
        // 震动效果的系统服务
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(30);//振动0.5秒
        // 下边是可以使震动有规律的震动  -1：表示不重复 0：循环的震动
    }
    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onResume() {
        startEnableBluetooth();
        super.onResume();
    }
    private void showLog(String text){
        Log.d("vsLog", "showLog: " + text);
    }
    protected void onDestroy() {
        super.onDestroy();
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
