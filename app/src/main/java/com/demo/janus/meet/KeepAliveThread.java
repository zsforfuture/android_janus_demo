package com.demo.janus.meet;

import com.demo.janus.handle.IKeepAliveHandle;

/**
 * @Author before
 * @Date 2020/12/7o
 * @desc
 */
public class KeepAliveThread implements Runnable {
    private boolean stop = false;
    private IKeepAliveHandle keepAlive;
    private String threadName;

    public KeepAliveThread(IKeepAliveHandle keepAlive, String threadName) {
        this.keepAlive = keepAlive;
        this.threadName = threadName;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    @Override
    public void run() {
        while (!stop) {
            Thread thread = Thread.currentThread();
            if (thread.getName().equals(threadName)) {
                try {
                    thread.sleep(25000);
                    keepAlive.sendKeepAlive();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
