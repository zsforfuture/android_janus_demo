package com.demo.janus.handle;

import com.demo.janus.bean.EventResponseBean;
import com.demo.janus.bean.PublisherBean;
import com.demo.janus.bean.ResponseBean;
import com.demo.janus.stream.SubscribeStream;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public interface ISubscribeHandle extends IHandle {

    String sendDetachMessage();

    boolean isDetachMessage(ResponseBean responseBean);

    boolean isCreateSubscribeHandle(ResponseBean responseBean);

    void createSubscribeHandle(ResponseBean response);

    void handleSubscribeEvent(EventResponseBean responseBean);

    SubscribeStream getSubscribeStream();

    void onSubscribeStreamReady(SubscribeStream subscribeStream);

    String getAttachTransaction();

    PublisherBean getPublisher();
}
