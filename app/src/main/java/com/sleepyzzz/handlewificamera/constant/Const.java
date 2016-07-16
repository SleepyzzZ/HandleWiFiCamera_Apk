package com.sleepyzzz.handlewificamera.constant;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-07-16
 * Time: 14:19
 * FIXME
 */
public class Const {

    public static final int DEFAULT_RTSP_PORT = 554;
    public static final int DEFAULT_CMDSERVER_PORT = 8080;
    public final static int DEFAULT_RECVPHOTO_PORT = 8000;

    public static final int DEFAULT_TIME_OUT = 5000;

    public static final int FAIL = -1;
    public final static int SUCCESS = 0;

    public final static int SOCKET_HEART_SECOND = 15;
    public final static int SOCKET_SLEEP_SECOND = 2;

    public final static int RECV_BUFFER_SIZE = 1400;

    public final static int MAX_ECHO_CMD_LENGTH = 100;
    public final static int MAX_PHOTO_NAME_LENGTH = 25;
    public final static int MAX_PHOTO_PATH_LENGTH = 48;

    public final static int 	CMD_SYN_TIME					= 0x01;
    public final static int		CMD_SYN_TIME_ECHO				= 0x02;

    public final static int 	CMD_TAKE_PHOTO					= 0x07;
    public final static int		CMD_ECHO_TAKE_PHOTO				= 0x08;

    public final static int LENGTH_INT = 4;
    public final static int LENGTH_BOOLEAN = 1;
    public final static int LENGTH_LONG = 8;
}
