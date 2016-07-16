package com.sleepyzzz.handlewificamera.socket;

import com.orhanobut.logger.Logger;
import com.sleepyzzz.handlewificamera.base.DTApplication;
import com.sleepyzzz.handlewificamera.constant.Const;
import com.sleepyzzz.handlewificamera.entity.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-07-16
 * Time: 15:39
 * FIXME
 */
public class SocketServerThread extends Thread {

    private final static String TAG = "SocketServerThread";

    private volatile boolean isStart = false;

    private volatile ServerSocket serverSocket;

    public void setStart(boolean start) {
        isStart = start;
    }

    public void stopThread() {

        this.isStart = false;
        try {

            serverSocket.close();
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();

        try {

            serverSocket = new ServerSocket(Const.DEFAULT_RECVPHOTO_PORT);
            while (isStart) {

                Socket clientSocket = serverSocket.accept();
                Logger.t(TAG).d("ServerSocket: %s", clientSocket.toString());

                Thread thread = new Thread(new Accptor(clientSocket));
                thread.start();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private static class Accptor implements Runnable {

        /*private final static String TAG = "Accptor";*/

        private Socket socket = null;

        public Accptor(Socket socket) {

            this.socket = socket;
        }

        private void pathIsExit(String path) {

            File file = new File(path);
            if (!file.exists()) {

                file.mkdirs();
            }
        }

        /*
         * <p>Title: run</p> <p>Description: </p>
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            // TODO Auto-generated method stub

            Date now = new Date();
            DateFormat df = DateFormat.getDateInstance();
            String date = df.format(now);
            String folderPath = DTApplication.mSDCardPath + "/SmartCamera/" + date;
            pathIsExit(folderPath);

            String photoName = new String(CmdEventHelper.getInstance().getPhotoName());
            String photoPath = folderPath + "/" + photoName;
            Logger.t(TAG).d(photoPath);

            DataInputStream dis = null;
            FileOutputStream fileOut = null;
            try {

                dis = new DataInputStream(socket.getInputStream());
                fileOut = new FileOutputStream(photoPath);

                int data_length = dis.readInt();
                int sum_length = data_length;
                Logger.t(TAG).d("picture size %d bytes", data_length);

                int read_buffer_length = 0;
                int need_to_read = 0;
                int have_read_num = 0;
                int read_num = 0;
                byte[] data = new byte[Const.RECV_BUFFER_SIZE];
                // 1400,1400个字节读取数据
                while (0 != data_length) {
                    if (data_length > Const.RECV_BUFFER_SIZE) {
                        read_buffer_length = Const.RECV_BUFFER_SIZE;
                    } else {
                        read_buffer_length = data_length;
                    }

                    need_to_read = read_buffer_length;
                    have_read_num = 0;
                    read_num = 0;
                    while (0 != read_buffer_length) {
                        read_num = dis.read(data, have_read_num, read_buffer_length);
                        read_buffer_length = read_buffer_length - read_num;
                        have_read_num = have_read_num + read_num;
                    }

                    data_length = data_length - need_to_read;
                    fileOut.write(data, 0, need_to_read);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {

                if (dis != null) {
                    try {

                        dis.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (fileOut != null) {
                    try {

                        fileOut.flush();
                        fileOut.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (socket != null) {
                    try {

                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            File imageFile = new File(photoPath);
            if (imageFile.exists()) {
                EventBus.getDefault().post(new MessageEvent.NotifyMediaEvent(photoPath));
            }
        }
    }
}