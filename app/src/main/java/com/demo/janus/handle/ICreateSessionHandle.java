package com.demo.janus.handle;

import com.demo.janus.bean.ResponseBean;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public interface ICreateSessionHandle {
    void sendCreateSession();

    boolean isCreateSessionResult(ResponseBean res);

    void onCreateSessionResult(ResponseBean res);
}
