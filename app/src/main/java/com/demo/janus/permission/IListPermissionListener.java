package com.demo.janus.permission;

import java.util.List;

/**
 * @Author before
 * @Date 2020/6/10
 * @desc
 */
public interface IListPermissionListener extends IPermissionListener {

    void onGranted();

    void onRefused(List<Permission> permissions);
}
