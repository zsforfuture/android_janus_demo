package com.demo.janus.handle;

import com.demo.janus.bean.EventResponseBean;
import com.demo.janus.bean.ResponseBean;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public interface IPublishHandle extends IHandle {

    void sendPublishMessage();

    boolean isCreatePublishHandle(ResponseBean response);

    void createPublishHandle(ResponseBean response);

    boolean isPublishEvent(EventResponseBean responseBean);

    void handlePublishEvent(EventResponseBean responseBean);
}
