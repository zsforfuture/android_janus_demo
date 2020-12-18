package com.demo.janus.bean;

import com.demo.janus.handle.ResponseMessageType;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public class ResponseBean implements Serializable {
    private String janus;
    private String transaction;
    private BigInteger session_id;
    private BigInteger handle_id;
    private BigInteger sender;
    private DataBean data;

    public ResponseMessageType getMessageType() {
        return ResponseMessageType.getMessageType(janus);
    }

    public String getJanus() {
        return janus;
    }

    public void setJanus(String janus) {
        this.janus = janus;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public BigInteger getSession_id() {
        return session_id;
    }

    public void setSession_id(BigInteger session_id) {
        this.session_id = session_id;
    }

    public BigInteger getHandle_id() {
        return handle_id;
    }

    public void setHandle_id(BigInteger handle_id) {
        this.handle_id = handle_id;
    }

    public BigInteger getSender() {
        return sender;
    }

    public void setSender(BigInteger sender) {
        this.sender = sender;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }
}
