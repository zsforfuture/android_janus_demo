package com.demo.janus.handle;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public interface IKeepAliveHandle {


    void sendKeepAlive();

    void startThread();

    void stopThread();
}
