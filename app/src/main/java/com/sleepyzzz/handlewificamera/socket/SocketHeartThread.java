package com.sleepyzzz.handlewificamera.socket;

import com.orhanobut.logger.Logger;
import com.sleepyzzz.handlewificamera.constant.Const;

import java.io.IOException;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * TODO-心跳包
 * Date: 2016-07-16
 * Time: 14:33
 * FIXME
 */
public class SocketHeartThread extends Thread {

    private static final String TAG = "SocketHeartThread";

    private volatile boolean isStop = true;

    public SocketHeartThread() {

    }

    public void setStop(boolean stop) {
        isStop = stop;
    }

    public void stopThread() {

        isStop = true;
        this.interrupt();
    }

    @Override
    public void run() {
        super.run();

        this.isStop = false;
        while(!isStop) {

            if(TcpClient.getInstance().isConnected()) {

                if(!TcpClient.getInstance().canConnectToServer()) {

                    reConnect();
                }

                try {

                    Thread.sleep(Const.SOCKET_HEART_SECOND * 1000);
                } catch (InterruptedException e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            } else {

                reConnect();
            }
        }
    }

    /**
     * Title: reConnect
     * Description: TODO-重新连接服务器，创建socket套接字
     * param @return    设定文件
     * return boolean    返回类型
     * throws
     */
    private boolean reConnect() {

        try {

            TcpClient.getInstance().reConnect();
            Logger.t(TAG).d("reconnect success");
        } catch (IOException e) {
            // TODO: handle exception
            Logger.t(TAG).d("reconnect fail");
            return false;
        }

        return true;
    }
}
