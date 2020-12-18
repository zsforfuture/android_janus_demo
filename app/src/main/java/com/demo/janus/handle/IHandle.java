package com.demo.janus.handle;

import org.json.JSONObject;

import java.math.BigInteger;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public interface IHandle {
    void sendAttachMessage();

    BigInteger getHandle();

    void sendLocalDescription(JSONObject sdpObj);

    void sendIceCandidate(JSONObject candidateObj);
}
