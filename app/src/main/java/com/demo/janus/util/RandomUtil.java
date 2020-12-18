package com.demo.janus.util;

import java.util.Random;

/**
 * @Author before
 * @Date 2020/11/27
 * @desc
 */
public class RandomUtil {
    final static String str = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    final static Random rnd = new Random();

    public static String randomString(Integer length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(str.charAt(rnd.nextInt(str.length())));
        }
        return sb.toString();
    }

    public static String randomString() {
        return randomString(12);
    }
}
