package com.demo.janus.meet;

import java.math.BigInteger;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public interface CreateSessionObserver {
    void onCreateSessionSuccess(BigInteger sessionId);

    void onCreateSessionFail(String error);
}
