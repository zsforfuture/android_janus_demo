package com.demo.janus.ws;

import com.demo.janus.constant.Constant;
import com.demo.janus.util.LogUtil;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.protocols.IProtocol;
import org.java_websocket.protocols.Protocol;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public class WebSocketConnection extends WebSocketClient {
    private static final String JANUS_PROTOCOL = "janus-protocol";

    private static volatile WebSocketConnection client;
    private static WebSocketCallback webSocketCallback = null;

    private WebSocketConnection(URI serverUri, Draft protocolDraft) {
        super(serverUri, protocolDraft);
    }

    public static void init(String url) {
        if (client == null) {
            synchronized (WebSocketConnection.class) {
                if (client == null) {
                    List<IProtocol> protocols = new ArrayList<>();
                    protocols.add(new Protocol(JANUS_PROTOCOL));
                    Draft_6455 proto_janus = new Draft_6455(Collections.emptyList(), protocols);
                    client = new WebSocketConnection(URI.create(url), proto_janus);
                }
            }
        }
    }

    public void setWebSocketCallback(WebSocketCallback callback) {
        webSocketCallback = callback;
    }

    public static WebSocketConnection getInstance() {
        if (client == null) {
            throw new RuntimeException("please first init WebSocketConnection()");
        }
        return client;
    }

    public void connectServer() {
        if (CHECK()) {
            LogUtil.d(Constant.SERVER_TAG, "connecting to server");
            super.connect();
        }
    }

    public void reconnect(){
        if (CHECK()) {
            LogUtil.d(Constant.SERVER_TAG, "reconnecting to server");
            super.reconnect();
        }
    }

    public void sendMessage(String msg) {
        if (CHECK()) {
            LogUtil.d(Constant.SERVER_TAG, "send to server:" + msg);
            if (isOpen()) {
                super.send(msg);
            }
        }
    }

    public void disconnectServer() {
        if (CHECK()) {
            super.close();
        }
    }


    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LogUtil.d(Constant.SERVER_TAG, "server is connected");
        if (webSocketCallback != null) {
            webSocketCallback.onConnected();
        }
    }

    @Override
    public void onMessage(String message) {
        LogUtil.d(Constant.SERVER_TAG, "receive from server:" + message);
        if (webSocketCallback != null) {
            webSocketCallback.onResponse(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LogUtil.d(Constant.SERVER_TAG, "server close:" + reason);
        if (webSocketCallback != null) {
            webSocketCallback.onDisconnect(code, reason, remote);
        }
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        LogUtil.d(Constant.SERVER_TAG, "server is error:" + ex.getMessage());
        if (webSocketCallback != null) {
            webSocketCallback.onError(ex.getMessage());
        }
    }

    private boolean CHECK() {
        if (client == null) {
            if (webSocketCallback != null) {
                webSocketCallback.onError("websocket client is null");
            }
            return false;
        }
        return true;
    }
}
