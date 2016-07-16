package com.sleepyzzz.handlewificamera.socket;

import com.sleepyzzz.handlewificamera.constant.Const;
import com.sleepyzzz.handlewificamera.entity.ServerInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-07-16
 * Time: 14:35
 * FIXME
 */
public class TcpClient {

    private static final String TAG = "TcpClient";

    private static TcpClient instance;

    private String serverIp;

    private int serverPort;

    private Socket mSocket;

    private DataInputStream dis;

    private DataOutputStream dos;

    private boolean isSocketInit = false;

    public TcpClient() {

        serverIp = ServerInfo.getInstance().getServerIp();
        serverPort = ServerInfo.getInstance().getServerPort();


        try {
            initSocket();
            isSocketInit = true;
        } catch (IOException e) {
            isSocketInit = false;
        }
    }

    private void initSocket() throws IOException {

        mSocket = new Socket();
        mSocket.setKeepAlive(true); //长连接
        mSocket.connect(new InetSocketAddress(serverIp, serverPort), Const.DEFAULT_TIME_OUT);
        dis = new DataInputStream(mSocket.getInputStream());
        dos = new DataOutputStream(mSocket.getOutputStream());
    }

    public static TcpClient getInstance() {

        if (instance == null) {
            synchronized (TcpClient.class) {
                if (instance == null) {
                    instance = new TcpClient();
                }
            }
        }

        return instance;
    }

    /**
     * 重新连接
     * @return
     * @throws IOException
     */
    public boolean reConnect() throws IOException {

        closeTcpClient();
        initSocket();
        isSocketInit = true;

        return isSocketInit;
    }

    /**
     * 关闭套接字
     */
    public void closeTcpClient() {

        if (null != mSocket) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (null != dis) {
            try {
                dis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (null != dos) {
            try {
                dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        isSocketInit = false;
    }

    /**
     * 发送消息
     * @param msg
     * @throws IOException
     */
    public void sendMsg(byte[] msg) throws IOException {

        if (null != dos) {
            dos.write(msg);
            dos.flush();
        }
    }

    /**
     * 接收消息
     * @param buffer
     * @param length
     * @return
     */
    public int recvMsg(byte[] buffer, int length) {

        int read_count = -1;
        int have_read_count = 0;

        if(dis != null) {

            while(have_read_count < length) {

                try {

                    read_count = mSocket.getInputStream()
                            .read(buffer, have_read_count, length-have_read_count);
                } catch (IOException e) {
                    // TODO: handle exception
                    e.printStackTrace();
                    return Const.FAIL;
                }

                if(read_count < 0) {

                    return Const.FAIL;
                } else {

                    have_read_count += read_count;

                }
            }
        }

        return have_read_count;
    }


    /**
     * 读取消息长度
     * @return
     */
    public int recvCmdLength() {

        int read_count = -1;

        if(null != dis) {

            try {

                read_count = dis.readInt();
            } catch (IOException e) {
                // TODO: handle exception
                e.printStackTrace();
                return Const.FAIL;
            }
        }

        return read_count;
    }

    /**
     * 发送心跳包
     * @return
     */
    public boolean canConnectToServer() {

        try {
            if(null != mSocket) {

                mSocket.sendUrgentData(0xff);
            }
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 判断连接是否正常
     * @return
     */
    public boolean isConnected() {

        boolean isConnect = false;
        if(isSocketInit && mSocket.isConnected() && !mSocket.isClosed())
            isConnect = true;

        return isConnect;
    }
}
