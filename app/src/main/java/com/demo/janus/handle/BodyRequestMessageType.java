package com.demo.janus.handle;

/**
 * @Author before
 * @Date 2020/12/9
 * @desc
 */
public enum BodyRequestMessageType {
    JOIN("join"),
    CONFIGURE("configure"),
    START("start"); // 订阅

    private final String type;

    BodyRequestMessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
