package com.sleepyzzz.handlewificamera.base;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.os.Environment;
import android.os.Vibrator;

import com.example.myapp.MyEventBusIndex;
import com.orhanobut.logger.Logger;
import com.sleepyzzz.handlewificamera.location.LocationService;

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

    public LocationService mLocationService;
    public Vibrator mVibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mSDCardPath = getSDCardPath();

        /**
         * Logger初始化
         */
        Logger.init();

        /**
         * eventbus添加索引
         */
        EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();

        /**
         * 初始化百度定位sdk
         */
        mLocationService = new LocationService(mContext);
        mVibrator = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
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
