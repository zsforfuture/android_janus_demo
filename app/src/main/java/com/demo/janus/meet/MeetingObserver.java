package com.demo.janus.meet;

import com.demo.janus.bean.PublisherBean;

import java.util.List;

/**
 * @Author before
 * @Date 2020/12/10
 * @desc
 */
public interface MeetingObserver {
    void onConnected();

    void onJoinRoom();

    void onUserInRoom(List<PublisherBean> users);

    void onUserJoin(List<PublisherBean> users);

    void onUserLeave(List<PublisherBean> users);

    void onDisconnect();
}
