package com.zt.vacss;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("BL_service", "进入了service的onStartCommand");
        return START_STICKY_COMPATIBILITY;
        //return super.onStartCommand(intent, flags, startId);

    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}