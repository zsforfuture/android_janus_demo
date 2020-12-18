package com.demo.janus.handle;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public enum SendMessageType {
    CREATE("create"),
    KEEP_ALIVE("keepalive"),
    ATTACH("attach"),
    DETACH("detach"),
    MESSAGE("message"),
    TRICKLE("trickle");

    private final String type;

    SendMessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
