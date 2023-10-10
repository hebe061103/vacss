package com.zt.vacss;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView menu_bt = findViewById(R.id.menu_img);
        menu_bt.setOnClickListener(view -> showPopupMenu(menu_bt));
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivityForResult(intent, 1);
            }else {

            }
        }else {
            // 如果是空的话，就别是当前设备不支持蓝牙
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    private void showPopupMenu(final View view) {
        final PopupMenu popupMenu = new PopupMenu(this,view);
        //menu 布局
        popupMenu.getMenuInflater().inflate(R.menu.main,popupMenu.getMenu());
        //点击事件
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.scan_bt) {
                Intent intent = new Intent(MainActivity.this,BleClientActivity.class);
                startActivities(new Intent[]{intent});
            } else if (itemId == R.id.about) {
                about();
            }
            return false;
        });
        //显示菜单，不要少了这一步
        popupMenu.show();
    }

    //拍照选择头像
    private void scanBtn() {
        //点击扫描蓝牙选项执行的逻辑
        Toast.makeText(this, "开始扫描蓝牙", Toast.LENGTH_SHORT).show();
    }

    //相册选择头像
    private void about() {
        //点击相册选项执行的逻辑
        Toast.makeText(this, "这里点击了关于", Toast.LENGTH_SHORT).show();
    }

}
