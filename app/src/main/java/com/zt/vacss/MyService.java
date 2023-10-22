package com.zt.vacss;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MyService extends Service {
    public MyService() {
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY_COMPATIBILITY;
        //return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public void onDestroy()
    {
        Intent localIntent = new Intent();
        localIntent.setClass(this, MyService.class); // 销毁时重新启动Service
        this.startService(localIntent);
    }
}