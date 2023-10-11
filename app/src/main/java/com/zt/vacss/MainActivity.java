package com.zt.vacss;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
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
    private void about() {
        //点击相册选项执行的逻辑
        Toast.makeText(this, "这里点击了关于", Toast.LENGTH_SHORT).show();
    }

}
