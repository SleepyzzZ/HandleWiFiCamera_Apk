package com.sleepyzzz.handlewificamera.entity;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-07-20
 * Time: 13:09
 * FIXME
 */
public class GpsInfo {

    private boolean isSucess;

    private double longitude;

    private double latitude;

    private static GpsInfo instance;

    public GpsInfo(boolean isSucess, double longitude, double latitude) {
        this.isSucess = isSucess;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public static GpsInfo getInstance() {
        if (instance == null) {
            synchronized (GpsInfo.class) {
                if (instance == null) {
                    instance = new GpsInfo(false, 0.0, 0.0);
                }
            }
        }

        return instance;
    }

    public boolean isSucess() {
        return isSucess;
    }

    public void setSucess(boolean sucess) {
        isSucess = sucess;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
