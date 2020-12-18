package com.demo.janus.ws;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public interface WebSocketCallback {
    void onConnected();

    void onResponse(String result);

    void onDisconnect(int code, String reason, boolean remote);

    void onError(String msg);
}
