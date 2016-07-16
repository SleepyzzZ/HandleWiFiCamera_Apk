package com.sleepyzzz.handlewificamera.socket;

import android.os.Bundle;

import com.orhanobut.logger.Logger;
import com.sleepyzzz.handlewificamera.constant.Const;
import com.sleepyzzz.handlewificamera.entity.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-07-16
 * Time: 14:57
 * FIXME
 */
public class CmdEventHelper {

    private final static String TAG = "CmdEventHelper";

    public static CmdEventHelper instance;

    private String photoName;

    private int echoPhotoSize;

    private byte[] echoPhotoName;

    public CmdEventHelper() {

        this.photoName = "";
        this.echoPhotoName = new byte[Const.MAX_PHOTO_NAME_LENGTH];
        this.echoPhotoSize = 0;
    }

    public static CmdEventHelper getInstance() {

        if (instance == null) {
            synchronized (CmdEventHelper.class) {
                if (instance == null) {
                    instance = new CmdEventHelper();
                }
            }
        }

        return instance;
    }

    public String getPhotoName() {

        return this.photoName;
    }



    /**
     * TODO-根据命令号生成发送消息包
     * @param cmd
     * @return
     */
    public byte[] generateCmdMsg(int cmd) {

        byte[] msg = null;
        switch (cmd) {
            case Const.CMD_SYN_TIME:
                msg = new byte[Const.LENGTH_INT + Const.LENGTH_INT
                        + Const.LENGTH_BOOLEAN + Const.LENGTH_LONG];
                System.arraycopy(
                        Int2ByteArray(Const.LENGTH_INT + Const.LENGTH_BOOLEAN
                                + Const.LENGTH_LONG, 4), 0, msg, 0,
                        Const.LENGTH_INT);
                System.arraycopy(Int2ByteArray(cmd, 4), 0, msg, Const.LENGTH_INT,
                        Const.LENGTH_INT);
                msg[Const.LENGTH_INT + Const.LENGTH_INT] = '0';
                System.arraycopy(Long2ByteArray(System.currentTimeMillis(), 8), 0,
                        msg, Const.LENGTH_INT + Const.LENGTH_INT
                                + Const.LENGTH_BOOLEAN, Const.LENGTH_LONG);
                break;
            case Const.CMD_TAKE_PHOTO:
                msg = new byte[Const.LENGTH_INT + Const.LENGTH_INT
                        + Const.LENGTH_BOOLEAN + Const.MAX_PHOTO_NAME_LENGTH
                        + Const.LENGTH_INT];
                System.arraycopy(
                        Int2ByteArray(Const.LENGTH_INT + Const.LENGTH_BOOLEAN
                                + Const.MAX_PHOTO_NAME_LENGTH + Const.LENGTH_INT, 4),
                        0, msg, 0, Const.LENGTH_INT);
                System.arraycopy(Int2ByteArray(cmd, 4), 0, msg, Const.LENGTH_INT,
                        Const.LENGTH_INT);
                msg[Const.LENGTH_INT + Const.LENGTH_INT] = '0';
                System.arraycopy(generatePhotoName(), 0, msg, Const.LENGTH_INT
                                + Const.LENGTH_INT + Const.LENGTH_BOOLEAN,
                        Const.MAX_PHOTO_NAME_LENGTH);
                System.arraycopy(Int2ByteArray(0, 4), 0, msg, Const.LENGTH_INT
                        + Const.LENGTH_INT + Const.LENGTH_BOOLEAN
                        + Const.MAX_PHOTO_NAME_LENGTH, 4);
                break;

            default:
                break;
        }

        return msg;
    }

    /**
     * Title: generatePhotoName
     * Description: TODO-生成传到下位机的图片名
     * param @return 设定文件
     * return byte[] 返回类型
     * throws
     */
    public byte[] generatePhotoName() {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd__HH-mm-ss",
                Locale.getDefault());
        photoName = df.format(new Date());
        photoName = photoName + ".jpeg";

        return photoName.getBytes();
    }

    private int handleEchoSynTime(byte[] msg) {

        if (1 != msg[Const.LENGTH_INT]) {

            return Const.FAIL;
        }

        return Const.SUCCESS;
    }

    /**
     * Title: handleEchoCmdMsg
     * Description: TODO-解析输入数据流
     * param @param msg
     * param @return 设定文件
     * return int 返回类型
     * throws
     */
    public int handleEchoCmdMsg(byte[] msg) {

        int cmd_number = ByteArray2Int(msg, 0);
        Logger.t(TAG).d("[handleEchoCmdMsg] cmd = %d", cmd_number);
        int result = -1;

        Bundle bundle = new Bundle();
        switch (cmd_number) {
            case Const.CMD_SYN_TIME_ECHO:
                result = handleEchoSynTime(msg);
                if (result == Const.SUCCESS) {
                    bundle.putString("CMD_SYN_TIME_ISSUCCEED",
                            "synchronize time success");
                } else {
                    bundle.putString("CMD_SYN_TIME_ISSUCCEED",
                            "synchronize time fail");
                }
                break;
            case Const.CMD_ECHO_TAKE_PHOTO:
                result = handleEchoTakePhoto(msg);
                if (result == Const.SUCCESS) {
                    bundle.putString("CMD_ECHO_TAKE_PHOTO_ISSUCCEED",
                            "capture sucess");
                    bundle.putByteArray("CMD_ECHO_TAKE_PHOTO_NAME",
                            this.echoPhotoName);
                    bundle.putInt("CMD_ECHO_TAKE_PHOTO_SIZE", this.echoPhotoSize);
                } else {
                    bundle.putString("CMD_ECHO_TAKE_PHOTO", "capture fail");
                    bundle.putByteArray("CMD_ECHO_TAKE_PHOTO_NAME", null);
                    bundle.putInt("CMD_ECHO_TAKE_PHOTO_SIZE", 0);
                }
                break;

            default:
                break;
        }
        EventBus.getDefault().post(new MessageEvent.EchoMsgEvent(cmd_number, bundle));

        return Const.SUCCESS;
    }

    private int handleEchoTakePhoto(byte[] msg) {

        if (1 == msg[Const.LENGTH_INT]) {

            Logger.t(TAG).d("handleEchoTakePhoto in");
            System.arraycopy(msg, Const.LENGTH_INT + Const.LENGTH_BOOLEAN,
                    this.echoPhotoName, 0, Const.MAX_PHOTO_NAME_LENGTH);
            this.echoPhotoSize = ByteArray2Int(msg, Const.LENGTH_INT
                    + Const.LENGTH_BOOLEAN + Const.MAX_PHOTO_NAME_LENGTH);
            String name = new String(this.echoPhotoName);
            Logger.t(TAG).d("[handleEchoTakePhoto] echoPhotoName = %s", name);
            Logger.t(TAG).d("[handleEchoTakePhoto] echoPhotoSize = %s", echoPhotoName);
        } else {

            return Const.FAIL;
        }

        return Const.SUCCESS;
    }

    /**
     * Title: Int2ByteArray
     * Description: TODO-int转byte[]
     * param @param iSource
     * param @param iArrayLen
     * param @return 设定文件
     * return byte[] 返回类型
     * throws
     */
    public byte[] Int2ByteArray(int iSource, int iArrayLen) {

        byte[] bLocalArr = new byte[iArrayLen];
        for (int i = 0; (i < 4) && (i < iArrayLen); i++) {
            bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);
        }

        return bLocalArr;
    }

    /**
     * Title: Long2ByteArray
     * Description: TODO
     * param @param iSource
     * param @param iArrayLen
     * param @return 设定文件
     * return byte[] 返回类型
     * throws
     */
    public byte[] Long2ByteArray(long iSource, int iArrayLen) {
        byte[] bLocalArr = new byte[iArrayLen];
        for (int i = 0; (i < 8) && (i < iArrayLen); i++) {
            bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);
        }

        return bLocalArr;
    }

    /**
     * Title: ByteArray2Int
     * Description: TODO-byte[]转int
     * param @param src
     * param @param srcPos
     * param @return 设定文件
     * return int 返回类型
     * throws
     */
    public int ByteArray2Int(byte[] src, int srcPos) {

        int result = 0;
        if (srcPos < 0 || src.length - srcPos < 4)
            return -1;

        for (int i = 0; i < 4; i++) {
            result += (src[i + srcPos] & 0xFF) << (8 * (3 - i));
        }

        return result;
    }
}
