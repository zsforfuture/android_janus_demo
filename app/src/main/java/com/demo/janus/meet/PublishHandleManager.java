package com.demo.janus.meet;

import com.demo.janus.bean.EventResponseBean;
import com.demo.janus.bean.JsepBean;
import com.demo.janus.bean.ResponseBean;
import com.demo.janus.handle.IPublishHandle;
import com.demo.janus.handle.imp.PublishHandle;
import com.demo.janus.stream.LocalStream;
import com.demo.janus.ws.WebSocketConnection;
import org.webrtc.SessionDescription;
import java.math.BigInteger;

/**
 * @Author before
 * @Date 2020/12/11
 * @desc
 */
class PublishHandleManager {

    private final ConferenceClientConfiguration configuration;
    private final WebSocketConnection connection;
    private final PublishObserver publishObserver;

    private MeetingOptions options;
    private BigInteger sessionId;

    private IPublishHandle publishHandle;
    private PublishPeerConnectionChannel publishChannel;

    public PublishHandleManager(WebSocketConnection connection, ConferenceClientConfiguration configuration, PublishObserver publishObserver) {
        this.configuration = configuration;
        this.connection = connection;
        this.publishObserver = publishObserver;
    }

    public void publish(LocalStream stream) {
        if (publishChannel == null) {
            publishChannel = new PublishPeerConnectionChannel(configuration.rtcConfiguration, publishHandle);
        }
        publishChannel.publish(stream);
    }

    public void unPublish() {
        if (publishChannel != null) {
            publishChannel.dispose();
        }
        publishHandle = null;
        publishChannel = null;
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

    public void createPublishHandle() {
        if (publishChannel == null) {
            publishHandle = new PublishHandle(connection, sessionId, options, publishObserver);
            publishHandle.sendAttachMessage();
        }
    }

    public void setRemoteDescription(JsepBean jsep) {
        if (publishChannel != null) {
            String sdpString = jsep.getSdp();
            SessionDescription remoteSdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(jsep.getType()),
                    sdpString);
            publishChannel.setRemoteDescription(remoteSdp);
        }
    }

    public boolean isCreatePublishHandle(ResponseBean response) {
        return publishHandle.isCreatePublishHandle(response);
    }

    public void createPublishHandle(ResponseBean response) {
        publishHandle.createPublishHandle(response);
    }

    public boolean isPublishEvent(EventResponseBean eventResponse) {
        return publishHandle.isPublishEvent(eventResponse);
    }

    public void handlePublishEvent(EventResponseBean eventResponse) {
        publishHandle.handlePublishEvent(eventResponse);
    }


}
