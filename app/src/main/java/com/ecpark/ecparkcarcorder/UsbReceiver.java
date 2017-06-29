package com.ecpark.ecparkcarcorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by yamei on 2017/6/7.
 */

public class UsbReceiver extends BroadcastReceiver {

    final String TAG="USBReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.MEDIA_UNMOUNTED")) {
            Log.v(TAG, "U盘拔出！");

            //关闭FTP服务器

        } else if (intent.getAction().equals("android.intent.action.MEDIA_MOUNTED")) {
            Log.v(TAG, "U盘插入！");

            //启动FTP服务器
        }
    }
}
