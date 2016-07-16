package com.sleepyzzz.handlewificamera.socket;

import com.orhanobut.logger.Logger;
import com.sleepyzzz.handlewificamera.entity.CmdEntity;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-07-16
 * Time: 15:25
 * FIXME
 */
public class SocketOutputThread extends Thread {

    private static final String TAG = "SocketOutputThread";

    private volatile boolean isStart = false;

    private List<CmdEntity> sendMsgList;

    public SocketOutputThread() {
        sendMsgList = new CopyOnWriteArrayList<CmdEntity>();    //并发容器
        sendMsgList.add(null);
    }

    public void setStart(boolean start) {

        isStart = start;
        synchronized (this) {		//同一时刻最多只有一个线程执行该段代码

            notify();
        }
    }

    public void stopThread() {

        this.isStart = false;
        this.interrupt();
    }

    /**
     * Title: addMsgToSendList
     * Description: TODO-将要发送的消息推入消息发送队列
     * param @param entity    设定文件
     * return void    返回类型
     * throws
     */
    public void addMsgToSendList(CmdEntity entity) {

        synchronized (this) {
            sendMsgList.add(entity);
            notify();
        }
    }

    /**
     * Title: sendMsg
     * Description: TODO-发送消息至服务端
     * param @param msg
     * param @return
     * param @throws IOException    设定文件
     * return boolean    返回类型
     * throws
     */
    public boolean sendMsg(byte[] msg) throws IOException {

        if(null == msg) {

            Logger.t(TAG).d("sendMsg is null");
            return false;
        }

        if(TcpClient.getInstance().isConnected()) {

            TcpClient.getInstance().sendMsg(msg);
            return true;
        }

        return false;
    }

    @Override
    public void run() {
        super.run();

        while(isStart) {
            synchronized (sendMsgList) {

                for(CmdEntity entity:sendMsgList) {

                    if(entity != null) {

                        try {

                            sendMsg(entity.getCmd());
                        } catch (Exception e) {
                            // TODO: handle exception
                            Logger.t(TAG).d("msg send error at syncronized send msglist");
                        } finally {

                            sendMsgList.remove(entity);
                        }
                    }
                }
            }
        }
    }
}
