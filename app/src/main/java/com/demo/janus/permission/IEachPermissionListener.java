package com.demo.janus.permission;

/**
 * @Author before
 * @Date 2020/6/10
 * @desc
 */
public interface IEachPermissionListener extends IPermissionListener{
    void onAccepted(Permission permission);
}
