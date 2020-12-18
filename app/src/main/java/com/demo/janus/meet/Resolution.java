package com.demo.janus.meet;

/**
 * @Author before
 * @Date 2020/12/17
 * @desc
 */
public enum Resolution {

    Resolution_720P(1280, 720),
    Resolution_480P(640, 480);

    public int width;
    public int height;

    Resolution(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
