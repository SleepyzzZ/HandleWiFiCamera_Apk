package com.sleepyzzz.handlewificamera.socket;

import com.orhanobut.logger.Logger;
import com.sleepyzzz.handlewificamera.constant.Const;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-07-16
 * Time: 15:16
 * FIXME
 */
public class SocketInputThread extends Thread {

    private static final String TAG = "SocketInputThread";

    private volatile boolean isStart = false;

    private byte[] recvBuffer;

    public SocketInputThread() {

    }

    public void setStart(boolean start) {
        isStart = start;
    }

    public void stopThread() {

        isStart = false;
        this.interrupt();
    }

    /**
     * Title: readEchoMsg
     * Description: TODO-读取来自相机端的返回消息(先接受命令包长度，防止tcp黏包)
     * param @return    设定文件
     * return int    返回类型
     * throws
     */
    private int recvEchoMsg() {

        int recvLen = TcpClient.getInstance().recvCmdLength();		//获得命令长度
        Logger.t(TAG).d("[recvEchoMsg] CmdLength: %d", recvLen);

        if(recvLen > 0) {

            recvBuffer = new byte[recvLen];
            int ret = TcpClient.getInstance().recvMsg(recvBuffer, recvLen);
            if(ret < 0) {

                return Const.FAIL;
            }
        }

        return Const.SUCCESS;
    }

    @Override
    public void run() {
        super.run();

        while(isStart) {
            if(!TcpClient.getInstance().isConnected()) {
                try {

                    Thread.sleep(Const.SOCKET_SLEEP_SECOND * 1000);
                } catch (InterruptedException e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            } else {

                if(recvEchoMsg() != Const.SUCCESS) {

                    TcpClient.getInstance().closeTcpClient();
                } else {

                    Logger.t(TAG).d("handle message");
                    CmdEventHelper.getInstance().handleEchoCmdMsg(recvBuffer);
                }
            }
        }
    }

}
