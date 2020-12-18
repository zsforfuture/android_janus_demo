package com.demo.janus.permission;

/**
 * @Author before
 * @Date 2020/6/10
 * @desc
 */
public interface ISimplePermissionListener extends IPermissionListener {
    void onAccepted(boolean allGranted);
}
