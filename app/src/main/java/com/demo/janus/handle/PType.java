package com.demo.janus.handle;

/**
 * @Author before
 * @Date 2020/12/9
 * @desc
 */
public enum PType {
    PUBLISHER("publisher"),
    SUBSCRIBER("subscriber");

    private final String type;

    PType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
