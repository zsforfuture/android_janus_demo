package com.demo.janus.handle;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public enum VideoRoomMessageType {
    JOINDE("joined"),
    EVENT("event"),
    ATTACHED("attached"); // 订阅返回事件

    private final String type;

    VideoRoomMessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static VideoRoomMessageType getVideoRoomMessageType(String type) {
        for (VideoRoomMessageType t : VideoRoomMessageType.values()) {
            if (t.getType().equals(type)) {
                return t;
            }
        }
        return null;
    }
}
