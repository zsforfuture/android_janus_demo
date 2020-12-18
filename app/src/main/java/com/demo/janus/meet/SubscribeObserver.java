package com.demo.janus.meet;

import com.demo.janus.bean.JsepBean;
import com.demo.janus.handle.ISubscribeHandle;
import com.demo.janus.stream.SubscribeStream;

import java.math.BigInteger;

/**
 * @Author before
 * @Date 2020/12/10
 * @desc
 */
public interface SubscribeObserver {
    void onCreateSubscribeHandleSuccess(BigInteger handle, ISubscribeHandle subscribeHandle);

    void onSubscribeSdp(BigInteger handle, JsepBean jsep);

    void onSubscribeStreamReady(BigInteger id, SubscribeStream stream);
}
