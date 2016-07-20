package com.sleepyzzz.handlewificamera.socket;

import android.media.ExifInterface;

import com.orhanobut.logger.Logger;
import com.sleepyzzz.handlewificamera.base.DTApplication;
import com.sleepyzzz.handlewificamera.constant.Const;
import com.sleepyzzz.handlewificamera.entity.GpsInfo;
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
                if (GpsInfo.getInstance().isSucess()) {
                    writeGpsToJpegExif(photoPath, GpsInfo.getInstance().getLatitude(),
                            GpsInfo.getInstance().getLongitude());
                }
            }
        }

        /**
         * 将gps信息写入jpeg文件的exif头中
         * @param path
         * @param latitude
         * @param longitude
         */
        private void writeGpsToJpegExif(String path, double latitude, double longitude) {

            try {
                ExifInterface exif = new ExifInterface(path);
                String tagLat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                String tagLng = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                if (tagLat == null && tagLng == null) {
                    exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,
                            decimalToDMS(latitude));
                    exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,
                            latitude > 0 ? "N" : "S");
                    exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,
                            decimalToDMS(longitude));
                    exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,
                            longitude > 0 ? "E" : "W");
                    //保存
                    exif.saveAttributes();
                }
            } catch (IOException e) {

            }
        }

        /**
         * 格式转化使gps正确写入exif
         *
         * @param value
         * @return
         */
        private String decimalToDMS(double value) {
            String output, degrees, minutes, seconds;

            // gets the modulus the coordinate divided by one (MOD1).
            // in other words gets all the numbers after the decimal point.
            // e.g. mod := -79.982195 % 1 == 0.982195
            //
            // next get the integer part of the coord. On other words the whole
            // number part.
            // e.g. intPart := -79

            double mod = value % 1;
            int intPart = (int) value;

            // set degrees to the value of intPart
            // e.g. degrees := "-79"

            degrees = String.valueOf(intPart);

            // next times the MOD1 of degrees by 60 so we can find the integer part
            // for minutes.
            // get the MOD1 of the new coord to find the numbers after the decimal
            // point.
            // e.g. coord := 0.982195 * 60 == 58.9317
            // mod := 58.9317 % 1 == 0.9317
            //
            // next get the value of the integer part of the coord.
            // e.g. intPart := 58

            value = mod * 60;
            mod = value % 1;
            intPart = (int) value;
            if (intPart < 0) {
                // Convert number to positive if it's negative.
                intPart *= -1;
            }

            // set minutes to the value of intPart.
            // e.g. minutes = "58"
            minutes = String.valueOf(intPart);

            // do the same again for minutes
            // e.g. coord := 0.9317 * 60 == 55.902
            // e.g. intPart := 55
            value = mod * 60;
            intPart = (int) value;
            if (intPart < 0) {
                // Convert number to positive if it's negative.
                intPart *= -1;
            }

            // set seconds to the value of intPart.
            // e.g. seconds = "55"
            seconds = String.valueOf(intPart);

            // I used this format for android but you can change it
            // to return in whatever format you like
            // e.g. output = "-79/1,58/1,56/1"
            output = degrees + "/1," + minutes + "/1," + seconds + "/1";

            // Standard output of D°M′S″
            // output = degrees + "°" + minutes + "'" + seconds + "\"";

            return output;
        }
    }
}