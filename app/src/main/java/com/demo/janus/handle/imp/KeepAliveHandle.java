package com.demo.janus.handle.imp;

import com.demo.janus.handle.IKeepAliveHandle;
import com.demo.janus.handle.MessageConstant;
import com.demo.janus.handle.SendMessageType;
import com.demo.janus.meet.KeepAliveThread;
import com.demo.janus.util.RandomUtil;
import com.demo.janus.ws.WebSocketConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public class KeepAliveHandle implements IKeepAliveHandle {

    private WebSocketConnection connection;
    private BigInteger sessionId;
    private Thread keepAliveThread;
    private KeepAliveThread keepAliveRunnable;

    public KeepAliveHandle(WebSocketConnection connection, BigInteger sessionId) {
        this.connection = connection;
        this.sessionId = sessionId;
        keepAliveRunnable = new KeepAliveThread(this, "KeepAlive");
        keepAliveThread = new Thread(keepAliveRunnable, "KeepAlive");
    }

    @Override
    public void startThread() {
        if (keepAliveThread != null) {
            keepAliveThread.start();
        }
    }

    @Override
    public void stopThread() {
        if (keepAliveRunnable != null) {
            keepAliveRunnable.setStop(true);
        }
    }

    @Override
    public void sendKeepAlive() {
        try {
            JSONObject obj = new JSONObject();
            obj.put(MessageConstant.JANUS, SendMessageType.KEEP_ALIVE.getType());
            obj.put(MessageConstant.SESSION_ID, sessionId);
            obj.put(MessageConstant.TRANSACTION, RandomUtil.randomString());
            connection.sendMessage(obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
