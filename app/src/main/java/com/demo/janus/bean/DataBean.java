package com.demo.janus.bean;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public class DataBean implements Serializable {
    private BigInteger id;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }
}
