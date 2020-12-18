package com.demo.janus.meet;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public interface JoinRoomObserver {
    void onSuccess();

    void onFail(String error);
}
