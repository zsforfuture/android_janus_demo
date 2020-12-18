package com.demo.janus.handle.imp;

import android.text.TextUtils;

import com.demo.janus.bean.ResponseBean;
import com.demo.janus.handle.ICreateSessionHandle;
import com.demo.janus.handle.MessageConstant;
import com.demo.janus.handle.SendMessageType;
import com.demo.janus.meet.CreateSessionObserver;
import com.demo.janus.util.RandomUtil;
import com.demo.janus.ws.WebSocketConnection;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public class CreateSessionHandle implements ICreateSessionHandle {
    private final WebSocketConnection connection;
    private final CreateSessionObserver observer;
    private final String transaction; // 保存创建的transaction，以便与后面服务器返回的做比较，如果一致说明是创建session事件

    public CreateSessionHandle(WebSocketConnection connection, CreateSessionObserver observer) {
        this.transaction = RandomUtil.randomString();
        this.connection = connection;
        this.observer = observer;
    }

    @Override
    public void sendCreateSession() {
        try {
            JSONObject obj = new JSONObject();
            obj.put(MessageConstant.JANUS, SendMessageType.CREATE.getType());
            obj.put(MessageConstant.TRANSACTION, transaction);
            connection.sendMessage(obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateSessionResult(ResponseBean res) {
        if (observer != null) {
            observer.onCreateSessionSuccess(res.getData().getId());
        }
    }

    @Override
    public boolean isCreateSessionResult(ResponseBean res) {
        return TextUtils.equals(transaction, res.getTransaction());
    }
}
