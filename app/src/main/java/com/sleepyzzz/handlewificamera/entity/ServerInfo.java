package com.sleepyzzz.handlewificamera.entity;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * TODO-存储服务器IP信息
 * Date: 2016-07-16
 * Time: 14:16
 * FIXME
 */
public class ServerInfo {

    private String serverIp;

    private int serverPort;

    private static volatile ServerInfo instance;

    public ServerInfo() {

    }

    public ServerInfo(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public static ServerInfo getInstance() {
        if (instance == null) {
            synchronized (ServerInfo.class) {
                if (instance == null) {
                    instance = new ServerInfo();
                }
            }
        }

        return instance;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setServerInfo(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }
}
