package com.ecpark.ecparkcarcorder;

import android.app.Service;
import android.content.Intent;
import android.util.Log;
import android.content.Context;
import android.os.IBinder;


/**
 * Created by yamei on 2017/6/6.
 */

public class NetService extends Service {
    private static final String TAG = "NetService";

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "Start service success");

        Context context = getApplicationContext();
        super.onCreate();
    }
}
