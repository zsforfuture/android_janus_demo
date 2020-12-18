package com.demo.janus.meet;

import com.demo.janus.bean.ResponseBean;
import com.demo.janus.handle.ICreateSessionHandle;
import com.demo.janus.handle.IKeepAliveHandle;
import com.demo.janus.handle.imp.CreateSessionHandle;
import com.demo.janus.handle.imp.KeepAliveHandle;
import com.demo.janus.ws.WebSocketConnection;

import java.math.BigInteger;

/**
 * @Author before
 * @Date 2020/12/11
 * @desc
 */
class ConnectHandleManager {
    private IKeepAliveHandle keepAliveHandle; // 保持websocket长链接
    private ICreateSessionHandle sessionHandle; // 创建sessionId

    private final WebSocketConnection connection;
    private final CreateSessionObserver observer;

    public ConnectHandleManager(WebSocketConnection connection, CreateSessionObserver observer) {
        this.connection = connection;
        this.observer = observer;
    }

    /**
     * 断开与服务器连接
     */
    public void disconnect() {
        if (keepAliveHandle != null) {
            keepAliveHandle.stopThread();
        }

        if (connection != null) {
            connection.disconnectServer();
        }

        keepAliveHandle = null;
        sessionHandle = null;
    }

    public void createSessionHandle() {
        if (sessionHandle == null) {
            sessionHandle = new CreateSessionHandle(connection, observer);
            sessionHandle.sendCreateSession();
        }
    }

    /**
     * 保持wbsocket长链接
     *
     * @param sessionId
     */
    public void createKeepAliveHandle(BigInteger sessionId) {
        if (keepAliveHandle == null) {
            keepAliveHandle = new KeepAliveHandle(connection, sessionId);
            keepAliveHandle.startThread();
        }
    }

    public boolean isCreateSessionResult(ResponseBean response) {
        return sessionHandle.isCreateSessionResult(response);
    }

    public void onCreateSessionResult(ResponseBean response) {
        sessionHandle.onCreateSessionResult(response);
    }
}
