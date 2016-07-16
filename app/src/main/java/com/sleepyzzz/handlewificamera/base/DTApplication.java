package com.sleepyzzz.handlewificamera.base;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.example.myapp.MyEventBusIndex;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-07-16
 * Time: 11:56
 * FIXME
 */
public class DTApplication extends Application {

    private static Context mContext;

    public static String mSDCardPath;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mSDCardPath = getSDCardPath();
        Logger.init();
        EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();
    }

    public static Context getContext() {

        return mContext;
    }

    private String getSDCardPath() {

        boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (hasSDCard)

            return Environment.getExternalStorageDirectory().toString();
        else

            return Environment.getDownloadCacheDirectory().toString();
    }
}
