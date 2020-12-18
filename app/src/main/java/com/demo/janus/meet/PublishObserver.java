package com.demo.janus.meet;

import com.demo.janus.bean.JsepBean;
import com.demo.janus.bean.PublisherBean;

import java.math.BigInteger;
import java.util.List;

/**
 * @Author before
 * @Date 2020/12/9
 * @desc
 */
public interface PublishObserver {

    void onCreatePublishHandleSuccess(BigInteger handle);

    void onExistPublishers(List<PublisherBean> list);

    void onPublishSdp(JsepBean jsep);

    void onUserJoin(List<PublisherBean> users);

    void onUserLeave(BigInteger id);
}
