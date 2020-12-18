package com.demo.janus.bean;

import java.io.Serializable;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public class JsepBean implements Serializable {
    private String type;
    private String sdp;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSdp() {
        return sdp;
    }

    public void setSdp(String sdp) {
        this.sdp = sdp;
    }
}
