package com.demo.janus.handle;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public enum ResponseMessageType {
    SUCCESS("success"),
    ACK("ack"),
    ATTACH("attach"),
    EVENT("event"),
    WEBRTCUP("webrtcup");

    private final String type;

    ResponseMessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static ResponseMessageType getMessageType(String type) {
        for (ResponseMessageType t : ResponseMessageType.values()) {
            if (t.getType().equals(type)) {
                return t;
            }
        }
        return null;
    }
}
