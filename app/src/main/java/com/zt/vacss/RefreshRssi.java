package com.zt.vacss;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class RefreshRssi extends Service  {
    private String bleToHere;
    public RefreshRssi() {
    }
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Log.d("server",">>>>>>>>>>service onCreate()<<<<<<<<<<<<");
        Toast.makeText(this, "服务已建立", Toast.LENGTH_LONG).show();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.d("server", ">>>>>>>>>>service onStart()<<<<<<<<<<<<<");
        Toast.makeText(this, "服务已启动", Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags, startId);

    }
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d("server",">>>>>>>>>>service onDestory()<<<<<<<<<<<<");
        Toast.makeText(this, "服务已停止", Toast.LENGTH_LONG).show();
    }
}
