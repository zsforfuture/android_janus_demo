package com.demo.janus.meet;

import com.demo.janus.bean.EventResponseBean;
import com.demo.janus.bean.JsepBean;
import com.demo.janus.bean.PublisherBean;
import com.demo.janus.bean.ResponseBean;
import com.demo.janus.handle.ISubscribeHandle;
import com.demo.janus.handle.imp.SubscribeHandle;
import com.demo.janus.stream.SubscribeStream;
import com.demo.janus.ws.WebSocketConnection;

import org.webrtc.RTCStatsCollectorCallback;
import org.webrtc.SessionDescription;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author before
 * @Date 2020/12/10
 * @desc
 */
class SubscribeHandleManager {
    private final ConcurrentHashMap<BigInteger, ISubscribeHandle> subscribeMap;
    private final ConcurrentHashMap<String, ISubscribeHandle> transactionMap;
    private final ConcurrentHashMap<BigInteger, SubscribePeerConnectionChannel> subscribePeerConnectionMap;
    private final List<PublisherBean> allUsers;

    private final ConferenceClientConfiguration configuration;
    private final WebSocketConnection connection;
    private MeetingOptions options;
    private BigInteger sessionId;
    private final SubscribeObserver observer;

    public SubscribeHandleManager(WebSocketConnection connection, ConferenceClientConfiguration configuration, SubscribeObserver observer) {
        this.connection = connection;
        this.observer = observer;
        this.configuration = configuration;
        allUsers = new ArrayList<>();
        subscribeMap = new ConcurrentHashMap<>();
        transactionMap = new ConcurrentHashMap<>();
        subscribePeerConnectionMap = new ConcurrentHashMap<>();
    }

    /**
     * 必须要调用
     *
     * @param options
     */
    public void setOptions(MeetingOptions options) {
        this.options = options;
    }

    /**
     * 必须要调用
     *
     * @param sessionId
     */
    public void setSessionId(BigInteger sessionId) {
        this.sessionId = sessionId;
    }

    public synchronized void subscribe(PublisherBean user, SubscribeStream stream) {
        ISubscribeHandle handle = generateSubscribeHandle(user, stream);
        if (transactionMap.get(handle.getAttachTransaction()) == null) {
            transactionMap.put(handle.getAttachTransaction(), handle);
            handle.sendAttachMessage();
        }
    }

    public void getStats(PublisherBean user, RTCStatsCollectorCallback callback) {
        for (ISubscribeHandle handle : subscribeMap.values()) {
            if (handle.getPublisher().equals(user)) {
                SubscribePeerConnectionChannel channel = subscribePeerConnectionMap.get(handle.getHandle());
                if (channel != null) {
                    channel.getConnectionStats(callback);
                }
                break;
            }
        }
    }

    /**
     * 发送detach命令
     *
     * @param user
     */
    public void unSubscribe(PublisherBean user) {
        for (ISubscribeHandle handle : subscribeMap.values()) {
            if (handle.getPublisher().equals(user)) {
                String transaction = handle.sendDetachMessage();
                transactionMap.put(transaction, handle);
                break;
            }
        }
    }

    public void unSubscribe(ISubscribeHandle subscribeHandle) {
        SubscribePeerConnectionChannel channel = subscribePeerConnectionMap.get(subscribeHandle.getHandle());
        if (channel != null) {
            channel.dispose();
            subscribeMap.remove(subscribeHandle.getHandle());
            subscribePeerConnectionMap.remove(subscribeHandle.getHandle());
        }
    }

    public void unSubscribeAll() {
        transactionMap.clear();
        subscribeMap.clear();
        subscribePeerConnectionMap.clear();
        allUsers.clear();
    }

    public void addSubscribeHandle(BigInteger handle, ISubscribeHandle subscribeHandle) {
        if (subscribeMap.get(handle) == null) {
            subscribeMap.put(handle, subscribeHandle);
        }

        if (subscribePeerConnectionMap.get(handle) == null) {
            SubscribePeerConnectionChannel connectionChannel = new SubscribePeerConnectionChannel(configuration.rtcConfiguration, subscribeHandle);
            connectionChannel.subscribe(subscribeHandle.getSubscribeStream());
            subscribePeerConnectionMap.put(handle, connectionChannel);
        }
    }


    /**
     * setRemoteDescription
     *
     * @param handle
     * @param jsep
     */
    public void setRemoteDescription(BigInteger handle, JsepBean jsep) {
        if (subscribePeerConnectionMap.get(handle) != null) {
            String sdpString = jsep.getSdp();
            SessionDescription remoteSdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(jsep.getType()),
                    sdpString);
            if (subscribePeerConnectionMap.get(handle) != null) {
                subscribePeerConnectionMap.get(handle).setRemoteDescription(remoteSdp);
            }
        }
    }

    private synchronized SubscribeHandle generateSubscribeHandle(PublisherBean user, SubscribeStream stream) {
        if (options == null || sessionId == null) {
            throw new RuntimeException("have not join Room!");
        }
        return new SubscribeHandle(connection, sessionId, user, options, stream, observer);
    }

    /**
     * 获取subscribe的Handle
     *
     * @param response
     */
    public synchronized void handleSuccessResponse(ResponseBean response) {
        ISubscribeHandle handle = transactionMap.get(response.getTransaction());
        if (handle != null) {
            if (handle.isDetachMessage(response)) {
                // detach 取消订阅
                unSubscribe(handle);
            } else if (handle.isCreateSubscribeHandle(response)) {
                // attach 创建handle
                handle.createSubscribeHandle(response);
            }
            transactionMap.remove(response.getTransaction());
        }
    }


    /**
     * 处理订阅端的event事件
     *
     * @param eventResponse
     */
    public synchronized void handleSubscribeEvent(EventResponseBean eventResponse) {
        ISubscribeHandle subscribeHandle = subscribeMap.get(eventResponse.getSender());
        if (subscribeHandle != null) {
            subscribeHandle.handleSubscribeEvent(eventResponse);
        }
    }

    public synchronized List<PublisherBean> addUser(List<PublisherBean> users) {
        // 去重
        List<PublisherBean> temp = new ArrayList<>();
        if (users != null && users.size() > 0) {
            for (PublisherBean user : users) {
                if (!allUsers.contains(user)) {
                    temp.add(user);
                }
            }
            allUsers.addAll(temp);
        }
        return temp;
    }

    public synchronized PublisherBean removeUser(BigInteger id) {
        PublisherBean bean = null;
        for (PublisherBean user : allUsers) {
            if (user.getId().equals(id)) {
                bean = user;
                break;
            }
        }
        if (bean != null) {
            allUsers.remove(bean);
        }
        return bean;
    }

    public synchronized void removeUser(List<PublisherBean> users) {

    }

    public List<PublisherBean> getAllUsers() {
        return allUsers;
    }

}
