package com.demo.janus.handle.imp;

import com.demo.janus.bean.EventResponseBean;
import com.demo.janus.bean.ResponseBean;
import com.demo.janus.handle.BodyRequestMessageType;
import com.demo.janus.handle.IPublishHandle;
import com.demo.janus.handle.PType;
import com.demo.janus.handle.SendMessageType;
import com.demo.janus.handle.VideoRoomMessageType;
import com.demo.janus.meet.MeetingOptions;
import com.demo.janus.meet.PublishObserver;
import com.demo.janus.util.RandomUtil;
import com.demo.janus.ws.WebSocketConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

import static com.demo.janus.handle.MessageConstant.BODY;
import static com.demo.janus.handle.MessageConstant.DISPLAY;
import static com.demo.janus.handle.MessageConstant.HANDLE_ID;
import static com.demo.janus.handle.MessageConstant.JANUS;
import static com.demo.janus.handle.MessageConstant.PLUGIN;
import static com.demo.janus.handle.MessageConstant.PLUGIN_VALUE;
import static com.demo.janus.handle.MessageConstant.PTYPE;
import static com.demo.janus.handle.MessageConstant.REQUEST;
import static com.demo.janus.handle.MessageConstant.ROOM;
import static com.demo.janus.handle.MessageConstant.SESSION_ID;
import static com.demo.janus.handle.MessageConstant.TRANSACTION;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public class PublishHandle implements IPublishHandle {
    private final WebSocketConnection connection;
    private final BigInteger sessionId;
    private final MeetingOptions options;
    private final String attachTransaction;
    private BigInteger handleId;
    private final PublishObserver observer;
    private final CommonHandle commonHandle;

    public PublishHandle(WebSocketConnection connection, BigInteger sessionId, MeetingOptions options, PublishObserver observer) {
        this.connection = connection;
        this.sessionId = sessionId;
        this.options = options;
        this.attachTransaction = RandomUtil.randomString();
        this.observer = observer;
        commonHandle = new CommonHandle(connection, sessionId, options);
    }

    @Override
    public void sendAttachMessage() {
        commonHandle.sendAttachMessage(sessionId, attachTransaction);
    }

    @Override
    public void sendPublishMessage() {
        try {
            JSONObject object = new JSONObject();
            object.put(JANUS, SendMessageType.MESSAGE.getType());
            object.put(PLUGIN, PLUGIN_VALUE);
            object.put(TRANSACTION, RandomUtil.randomString());
            object.put(SESSION_ID, sessionId);
            object.put(HANDLE_ID, handleId);

            JSONObject body = new JSONObject();
            body.put(REQUEST, BodyRequestMessageType.JOIN.getType());
            body.put(PTYPE, PType.PUBLISHER.getType());
            body.put(ROOM, options.meetNum);
            body.put(DISPLAY, options.userName);
            object.put(BODY, body);

            connection.sendMessage(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendLocalDescription(JSONObject sdpObj) {
        commonHandle.sendLocalDescription(handleId, sdpObj);
    }

    @Override
    public void sendIceCandidate(JSONObject candidateObj) {
        commonHandle.sendIceCandidate(handleId, candidateObj);
    }

    @Override
    public boolean isCreatePublishHandle(ResponseBean bean) {
        return attachTransaction.equals(bean.getTransaction());
    }

    @Override
    public void createPublishHandle(ResponseBean response) {
        handleId = response.getData().getId();
        if (observer != null) {
            observer.onCreatePublishHandleSuccess(handleId);
        }
        sendPublishMessage();
    }

    @Override
    public BigInteger getHandle() {
        return handleId;
    }

    @Override
    public boolean isPublishEvent(EventResponseBean responseBean) {
        if (responseBean.getSender() == null) return false;
        return handleId.equals(responseBean.getSender());
    }

    @Override
    public void handlePublishEvent(EventResponseBean responseBean) {
        if (responseBean.getPlugindata() == null) {
            // TODO 出错
            return;
        }

        VideoRoomMessageType type = responseBean.getPlugindata().getData().getVideoRoomMessageType();
        switch (type) {
            case EVENT:
                if (observer != null) {
                    // {"plugindata":{"data":{"room":1234,"videoroom":"event"},"plugin":"janus.plugin.videoroom"},"janus":"event","sender":3300433881381405,"session_id":1845820824811854}
                    // 如果有leaving字段，说明是有人离开会议
                    if (responseBean.getPlugindata().getData().getLeaving() != null) {
                        observer.onUserLeave(responseBean.getPlugindata().getData().getLeaving());
                        return;
                    }

                    // 如果有publisher说明是会中加入人员
                    if (responseBean.getPlugindata() != null && responseBean.getPlugindata().getData().getPublishers() != null) {
                        observer.onUserJoin(responseBean.getPlugindata().getData().getPublishers());
                    }

                    // 加入会议时已经在会中的人员
                    // 处理服务器返回sdp
                    if (responseBean.getJsep() != null) {
                        observer.onPublishSdp(responseBean.getJsep());
                    }
                }
                break;
            case JOINDE:
                if (observer != null) {
                    // 加入会议时已经在会中的人员
                    observer.onExistPublishers(responseBean.getPlugindata().getData().getPublishers());
                }
                break;
        }
    }
}
