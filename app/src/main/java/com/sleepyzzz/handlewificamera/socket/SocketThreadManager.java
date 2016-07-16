package com.sleepyzzz.handlewificamera.socket;

import com.sleepyzzz.handlewificamera.entity.CmdEntity;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-07-16
 * Time: 16:07
 * FIXME
 */
public class SocketThreadManager {

    private static final String TAG = "SocketThreadManager";

    private SocketHeartThread mHeartThread;

    private SocketInputThread mInputThread;

    private SocketOutputThread mOutputThread;

    private SocketServerThread mServerThread;

    private static SocketThreadManager instance;

    public static SocketThreadManager getInstance() {

        if(instance == null) {
            synchronized (SocketThreadManager.class) {
                if(instance == null) {
                    instance = new SocketThreadManager();
                }
            }
        }
        return instance;
    }

    public SocketThreadManager() {
        // TODO Auto-generated constructor stub
        mHeartThread = new SocketHeartThread();
        mHeartThread.setStop(false);
        mHeartThread.start();

        mInputThread = new SocketInputThread();
        mInputThread.setStart(true);
        mInputThread.start();

        mOutputThread = new SocketOutputThread();
        mOutputThread.setStart(true);
        mOutputThread.start();

        mServerThread = new SocketServerThread();
        mServerThread.setStart(true);
        mServerThread.start();
    }

    public void stopThread() {

        mHeartThread.stopThread();

        mInputThread.stopThread();

        mOutputThread.stopThread();

        mServerThread.stopThread();
    }

    public static void releaseInstance() {

        if(instance != null) {

            instance.stopThread();
            instance = null;
        }
    }

    public void sendMsg(byte[] buffer) {

        CmdEntity entity = new CmdEntity(buffer);
        mOutputThread.addMsgToSendList(entity);
    }
}
