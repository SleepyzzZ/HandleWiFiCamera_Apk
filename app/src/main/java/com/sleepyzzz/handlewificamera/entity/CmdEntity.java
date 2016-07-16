package com.sleepyzzz.handlewificamera.entity;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Description: TODO-存储命令实体
 * Date: 2016-07-16
 * Time: 14:11
 * FIXME
 */
public class CmdEntity {

    private byte[] cmd;

    public CmdEntity(byte[] cmd) {
        this.cmd = cmd;
    }

    public byte[] getCmd() {
        return cmd;
    }

    public void setCmd(byte[] cmd) {
        this.cmd = cmd;
    }
}
