package com.demo.janus.meet;

import android.os.Handler;
import android.os.Looper;

import com.demo.janus.bean.EventResponseBean;
import com.demo.janus.bean.JsepBean;
import com.demo.janus.bean.PublisherBean;
import com.demo.janus.bean.ResponseBean;
import com.demo.janus.handle.ISubscribeHandle;
import com.demo.janus.handle.MessageParser;
import com.demo.janus.handle.ResponseMessageType;
import com.demo.janus.stream.LocalStream;
import com.demo.janus.stream.SubscribeStream;
import com.demo.janus.util.LogUtil;
import com.demo.janus.ws.WebSocketCallback;
import com.demo.janus.ws.WebSocketConnection;

import org.webrtc.RTCStatsCollectorCallback;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.demo.janus.constant.Constant.SERVER_TAG;
import static com.demo.janus.constant.Constant.SERVER_URL;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
class MeetingService implements WebSocketCallback, CreateSessionObserver
        , PublishObserver, SubscribeObserver {
    private static final String URL = SERVER_URL;

    private boolean serverConnected;
    private final ConnectHandleManager connectHandleManager;
    private final PublishHandleManager publishHandleManager;
    private final SubscribeHandleManager subscribeHandleManager;
    private final WebSocketConnection webSocketConnection;
    private final ConcurrentHashMap<BigInteger, ISubscribeAck> subscribeAcks;
    private MeetingObserver meetingObserver;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public MeetingService(ConferenceClientConfiguration configuration) {
        WebSocketConnection.init(URL);
        webSocketConnection = WebSocketConnection.getInstance();
        webSocketConnection.setWebSocketCallback(this);
        webSocketConnection.connectServer();
        connectHandleManager = new ConnectHandleManager(webSocketConnection, this);
        subscribeHandleManager = new SubscribeHandleManager(webSocketConnection, configuration, this);
        publishHandleManager = new PublishHandleManager(webSocketConnection, configuration, this);
        subscribeAcks = new ConcurrentHashMap<>();
    }


    /**
     * websocket 断开连接只能重连，不能再次调用connect()方法
     */
    public void reconnect() {
        if (webSocketConnection.isClosed()) {
            webSocketConnection.reconnect();
        }
    }

    public void joinRoom(MeetingOptions options) {
        CHECK_CONNECT();
        subscribeHandleManager.setOptions(options);
        publishHandleManager.setOptions(options);
        connectHandleManager.createSessionHandle();
    }

    public void publish(LocalStream stream) {
        CHECK_CONNECT();
        publishHandleManager.publish(stream);
    }


    public void subscribe(SubscribeStream stream, PublisherBean user, ISubscribeAck ack) {
        CHECK_CONNECT();
        if (user == null) return;
        subscribeAcks.put(user.getId(), ack);
        subscribeHandleManager.subscribe(user, stream);
    }

    public void getStats(PublisherBean user, RTCStatsCollectorCallback callback) {
        subscribeHandleManager.getStats(user, callback);
    }

    public void unSubscribe(PublisherBean user) {
        subscribeAcks.remove(user.getId());
        subscribeHandleManager.unSubscribe(user);
    }

    public void leaveRoom() {
        // 取消所有订阅
        subscribeHandleManager.unSubscribeAll();
        // 取消发布
        publishHandleManager.unPublish();
        // 断开服务器连接
        connectHandleManager.disconnect();
    }

    public List<PublisherBean> getAllUsers() {
        return subscribeHandleManager.getAllUsers();
    }

    public void setMeetingObserver(MeetingObserver meetingObserver) {
        this.meetingObserver = meetingObserver;
    }

    /**
     * ------------------------------ WebSocketCallback ----------------------------------------
     */
    @Override
    public void onConnected() {
        serverConnected = true;
        if (meetingObserver != null) {
            mHandler.post(() -> meetingObserver.onConnected());
        }
    }

    @Override
    public void onResponse(String result) {
        ResponseBean response = MessageParser.parser(result);
        if (response == null) {
            // TODO 服务器返回错误，待处理
            return;
        }
        ResponseMessageType type = response.getMessageType();
        switch (type) {
            case ACK:
                //TODO 保持长链接的响应，暂不处理
                break;
            case WEBRTCUP:
                //TODO sdp建立连接成功
                break;
            case SUCCESS:
                if (connectHandleManager.isCreateSessionResult(response)) {
                    // 获取会话ID sessionId
                    connectHandleManager.onCreateSessionResult(response);
                } else if (publishHandleManager.isCreatePublishHandle(response)) {
                    // publish端获取Handle
                    publishHandleManager.createPublishHandle(response);
                } else {
                    // subscribe端获取Handle
                    subscribeHandleManager.handleSuccessResponse(response);
                }
                break;
            case EVENT:
                EventResponseBean eventResponse = (EventResponseBean) response;
                if (publishHandleManager.isPublishEvent(eventResponse)) {
                    // 处理publish event事件
                    publishHandleManager.handlePublishEvent(eventResponse);
                } else {
                    // 处理subscribe event事件
                    subscribeHandleManager.handleSubscribeEvent(eventResponse);
                }
                break;
        }
    }

    @Override
    public void onDisconnect(int code, String reason, boolean remote) {
        LogUtil.e(SERVER_TAG, "meeting service onDisconnect--->" + reason);
        serverConnected = false;

        if (meetingObserver != null) {
            mHandler.post(() -> meetingObserver.onDisconnect());
        }
    }

    @Override
    public void onError(String msg) {
        LogUtil.e(SERVER_TAG, "meeting service onError--->" + msg);
        // serverConnected = false;
    }


    /**
     * -----------------------------------CreateSessionObserver------------------------------
     */
    @Override
    public void onCreateSessionSuccess(BigInteger sessionId) {
        LogUtil.d(SERVER_TAG, "onCreateSessionSuccess");

        subscribeHandleManager.setSessionId(sessionId);
        publishHandleManager.setSessionId(sessionId);

        // 开始建立publish端
        publishHandleManager.createPublishHandle();

        // 保持长链接
        connectHandleManager.createKeepAliveHandle(sessionId);
    }

    @Override
    public void onCreateSessionFail(String error) {

    }

    /**
     * ------------------------------------PublishObserver--------------------------
     */
    @Override
    public void onCreatePublishHandleSuccess(BigInteger handle) {
        LogUtil.d(SERVER_TAG, "-------->onCreatePublishHandleSuccess:" + handle);
        if (meetingObserver != null) {
            mHandler.post(() -> meetingObserver.onJoinRoom());
        }
    }

    @Override
    public void onExistPublishers(List<PublisherBean> list) {
        subscribeHandleManager.addUser(list);
        if (meetingObserver != null) {
            mHandler.post(() -> meetingObserver.onUserInRoom(subscribeHandleManager.getAllUsers()));
        }
    }

    @Override
    public void onPublishSdp(JsepBean jsep) {
        publishHandleManager.setRemoteDescription(jsep);
    }

    @Override
    public void onUserJoin(List<PublisherBean> users) {
        List<PublisherBean> temp = subscribeHandleManager.addUser(users);
        if (meetingObserver != null && temp.size() > 0) {
            mHandler.post(() -> meetingObserver.onUserJoin(temp));
        }
    }

    @Override
    public void onUserLeave(BigInteger id) {
        if (meetingObserver != null) {
            PublisherBean user = subscribeHandleManager.removeUser(id);
            if (user != null) {
                List<PublisherBean> users = new ArrayList<>();
                users.add(user);
                mHandler.post(() -> meetingObserver.onUserLeave(users));
            }
        }
    }

    /**
     * -------------------------------SubscribeObserver--------------------------------
     */
    @Override
    public void onCreateSubscribeHandleSuccess(BigInteger handle, ISubscribeHandle subscribeHandle) {
        LogUtil.d(SERVER_TAG, "MeetingService---->onCreateSubscribeHandleSuccess:" + handle + "--subscribeHandle== null" + (subscribeHandle == null));
        subscribeHandleManager.addSubscribeHandle(handle, subscribeHandle);
    }

    @Override
    public void onSubscribeSdp(BigInteger handle, JsepBean jsep) {
        subscribeHandleManager.setRemoteDescription(handle, jsep);
    }

    @Override
    public void onSubscribeStreamReady(BigInteger id, SubscribeStream stream) {
        LogUtil.d(SERVER_TAG, "MeetingService---->onSubscribeStreamReady");
        if (subscribeAcks.get(id) != null) {
            subscribeAcks.get(id).onSubscribeStreamReady();
            subscribeAcks.remove(id);
        }
    }

    private void CHECK_CONNECT() {
        if (!serverConnected) {
            throw new RuntimeException("server is not connected");
        }
    }
}
