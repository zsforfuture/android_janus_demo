package com.demo.janus.handle.imp;

import com.demo.janus.handle.BodyRequestMessageType;
import com.demo.janus.handle.SendMessageType;
import com.demo.janus.meet.MeetingOptions;
import com.demo.janus.util.RandomUtil;
import com.demo.janus.ws.WebSocketConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

import static com.demo.janus.handle.MessageConstant.AUDIO;
import static com.demo.janus.handle.MessageConstant.BODY;
import static com.demo.janus.handle.MessageConstant.CANDIDATE;
import static com.demo.janus.handle.MessageConstant.HANDLE_ID;
import static com.demo.janus.handle.MessageConstant.JANUS;
import static com.demo.janus.handle.MessageConstant.JESP;
import static com.demo.janus.handle.MessageConstant.PLUGIN;
import static com.demo.janus.handle.MessageConstant.PLUGIN_VALUE;
import static com.demo.janus.handle.MessageConstant.REQUEST;
import static com.demo.janus.handle.MessageConstant.SESSION_ID;
import static com.demo.janus.handle.MessageConstant.TRANSACTION;
import static com.demo.janus.handle.MessageConstant.VIDEO;

/**
 * @Author before
 * @Date 2020/12/9
 * @desc
 */
class CommonHandle {
    private final WebSocketConnection connection;
    private final BigInteger sessionId;
    private final MeetingOptions options;

    public CommonHandle(WebSocketConnection connection,
                        BigInteger sessionId, MeetingOptions options) {
        this.connection = connection;
        this.sessionId = sessionId;
        this.options = options;
    }

    void sendAttachMessage(BigInteger sessionId, String transaction) {
        try {
            JSONObject object = new JSONObject();
            object.put(JANUS, SendMessageType.ATTACH.getType());
            object.put(PLUGIN, PLUGIN_VALUE);
            object.put(TRANSACTION, transaction);
            object.put(SESSION_ID, sessionId);
            connection.sendMessage(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void sendLocalDescription(BigInteger handleId, JSONObject sdpObj) {
        try {
            JSONObject msg = new JSONObject();
            JSONObject body = new JSONObject();
            body.put(REQUEST, BodyRequestMessageType.CONFIGURE.getType());
            body.put(AUDIO, options.audio);
            body.put(VIDEO, options.video);

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

    void sendIceCandidate(BigInteger handleId, JSONObject candidateObj) {
        try {
            JSONObject msg = new JSONObject();
            msg.put(JANUS, SendMessageType.TRICKLE.getType());
            msg.put(TRANSACTION, RandomUtil.randomString());
            msg.put(SESSION_ID, sessionId);
            msg.put(HANDLE_ID, handleId);
            msg.put(CANDIDATE, candidateObj);
            connection.sendMessage(msg.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
