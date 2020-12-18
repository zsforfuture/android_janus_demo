package com.demo.janus.handle;

import android.text.TextUtils;

import com.demo.janus.bean.EventResponseBean;
import com.demo.janus.bean.ResponseBean;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public class MessageParser {

    public static ResponseBean parser(String msg) {
        if (TextUtils.isEmpty(msg)) return null;

        String type = null;
        try {
            JSONObject obj = new JSONObject(msg);
            if (obj.has(MessageConstant.JANUS)) {
                type = obj.getString(MessageConstant.JANUS);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ResponseMessageType messageType = ResponseMessageType.getMessageType(type);
        if (messageType == null) return null;
        switch (messageType) {
            case ACK:
            case SUCCESS:
                return new Gson().fromJson(msg, ResponseBean.class);
            case EVENT:
                return new Gson().fromJson(msg, EventResponseBean.class);
        }
        return null;
    }
}
