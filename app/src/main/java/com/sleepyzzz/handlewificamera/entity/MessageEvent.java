package com.sleepyzzz.handlewificamera.entity;

import android.os.Bundle;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-07-16
 * Time: 20:59
 * FIXME
 */
public class MessageEvent {

    public static class EchoMsgEvent {

        private int echoCode;

        private Bundle echoMsg;

        public EchoMsgEvent(int echoCode, Bundle echoMsg) {
            this.echoCode = echoCode;
            this.echoMsg = echoMsg;
        }

        public int getEchoCode() {
            return echoCode;
        }

        public Bundle getEchoMsg() {
            return echoMsg;
        }
    }

    public static class NotifyMediaEvent {

        private String photoPath;

        public NotifyMediaEvent(String photoPath) {
            this.photoPath = photoPath;
        }

        public String getPhotoPath() {
            return photoPath;
        }
    }
}
