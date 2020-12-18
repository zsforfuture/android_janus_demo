package com.demo.janus.handle.imp;

import com.demo.janus.bean.EventResponseBean;
import com.demo.janus.bean.PublisherBean;
import com.demo.janus.bean.ResponseBean;
import com.demo.janus.handle.BodyRequestMessageType;
import com.demo.janus.handle.ISubscribeHandle;
import com.demo.janus.handle.PType;
import com.demo.janus.handle.SendMessageType;
import com.demo.janus.handle.VideoRoomMessageType;
import com.demo.janus.meet.MeetingOptions;
import com.demo.janus.meet.SubscribeObserver;
import com.demo.janus.stream.SubscribeStream;
import com.demo.janus.util.RandomUtil;
import com.demo.janus.ws.WebSocketConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

import static com.demo.janus.handle.MessageConstant.BODY;
import static com.demo.janus.handle.MessageConstant.FEED;
import static com.demo.janus.handle.MessageConstant.HANDLE_ID;
import static com.demo.janus.handle.MessageConstant.JANUS;
import static com.demo.janus.handle.MessageConstant.JESP;
import static com.demo.janus.handle.MessageConstant.PLUGIN;
import static com.demo.janus.handle.MessageConstant.PLUGIN_VALUE;
import static com.demo.janus.handle.MessageConstant.PTYPE;
import static com.demo.janus.handle.MessageConstant.REQUEST;
import static com.demo.janus.handle.MessageConstant.ROOM;
import static com.demo.janus.handle.MessageConstant.SESSION_ID;
import static com.demo.janus.handle.MessageConstant.TRANSACTION;

/**
 * @Author before
 * @Date 2020/12/10
 * @desc
 */
public class SubscribeHandle implements ISubscribeHandle {
    private final WebSocketConnection connection;
    private final BigInteger sessionId;
    private final MeetingOptions options;
    private final PublisherBean publisherBean;
    private final String attachTransaction;
    private String detachTransaction = null;
    private BigInteger handleId;
    private final SubscribeObserver observer;
    private final CommonHandle commonHandle;
    private SubscribeStream subscribeStream;

    public SubscribeHandle(WebSocketConnection connection, BigInteger sessionId, PublisherBean publisherBean,
                           MeetingOptions options, SubscribeStream stream, SubscribeObserver observer) {
        this.connection = connection;
        this.sessionId = sessionId;
        this.options = options;
        this.publisherBean = publisherBean;
        this.attachTransaction = RandomUtil.randomString();
        this.observer = observer;
        this.subscribeStream = stream;
        commonHandle = new CommonHandle(connection, sessionId, options);
    }

    @Override
    public void sendAttachMessage() {
        commonHandle.sendAttachMessage(sessionId, attachTransaction);
    }

    @Override
    public String sendDetachMessage() {
        try {
            detachTransaction = RandomUtil.randomString();
            JSONObject object = new JSONObject();
            object.put(JANUS, SendMessageType.DETACH.getType());
            object.put(TRANSACTION, detachTransaction);
            object.put(SESSION_ID, sessionId);
            object.put(HANDLE_ID, handleId);
            connection.sendMessage(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return detachTransaction;
    }

    @Override
    public boolean isDetachMessage(ResponseBean responseBean) {
        return responseBean.getTransaction().equals(detachTransaction);
    }

    private void sendSubscribeMessage() {
        try {
            JSONObject object = new JSONObject();
            object.put(JANUS, SendMessageType.MESSAGE.getType());
            object.put(PLUGIN, PLUGIN_VALUE);
            object.put(TRANSACTION, RandomUtil.randomString());
            object.put(SESSION_ID, sessionId);
            object.put(HANDLE_ID, handleId);

            JSONObject body = new JSONObject();
            body.put(REQUEST, BodyRequestMessageType.JOIN.getType());
            body.put(PTYPE, PType.SUBSCRIBER.getType());
            body.put(ROOM, options.meetNum);
            body.put(FEED, publisherBean.getId());
            object.put(BODY, body);
            connection.sendMessage(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendLocalDescription(JSONObject sdpObj) {
        try {
            JSONObject msg = new JSONObject();
            JSONObject body = new JSONObject();
            body.put(REQUEST, BodyRequestMessageType.START.getType());
            body.put(ROOM, options.meetNum);

            msg.put(JANUS, SendMessageType.MESSAGE.getType());
            msg.put(TRANSACTION, RandomUtil.randomString());
            msg.put(SESSION_ID, sessionId);
            msg.put(HANDLE_ID, handleId);

            msg.put(BODY, body);
            msg.put(JESP, sdpObj);
            connection.sendMessage(msg.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendIceCandidate(JSONObject candidateObj) {
        commonHandle.sendIceCandidate(handleId, candidateObj);
    }


    @Override
    public void createSubscribeHandle(ResponseBean response) {
        handleId = response.getData().getId();
        if (observer != null) {
            observer.onCreateSubscribeHandleSuccess(handleId, this);
        }
        sendSubscribeMessage();
    }

    @Override
    public boolean isCreateSubscribeHandle(ResponseBean responseBean) {
        return attachTransaction.equals(responseBean.getTransaction());
    }

    @Override
    public void handleSubscribeEvent(EventResponseBean responseBean) {
        if (responseBean.getPlugindata() == null) {
            // TODO 出错
            return;
        }

        VideoRoomMessageType type = responseBean.getPlugindata().getData().getVideoRoomMessageType();
        switch (type) {
            case EVENT:
                //TODO 订阅端 event事件 待处理
                break;
            case ATTACHED:
                if (observer != null) {
                    if (responseBean.getJsep() != null) {
                        observer.onSubscribeSdp(handleId, responseBean.getJsep());
                    }
                }
                break;
        }
    }

    @Override
    public BigInteger getHandle() {
        return handleId;
    }

    @Override
    public PublisherBean getPublisher() {
        return publisherBean;
    }

    @Override
    public String getAttachTransaction() {
        return attachTransaction;
    }

    @Override
    public SubscribeStream getSubscribeStream() {
        return subscribeStream;
    }


    @Override
    public void onSubscribeStreamReady(SubscribeStream subscribeStream) {
        if (observer != null) {
            observer.onSubscribeStreamReady(publisherBean.getId(), subscribeStream);
        }
    }
}
