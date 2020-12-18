package com.demo.janus.permission;

/**
 * @Author before
 * @Date 2020/6/10
 * @desc
 */
public class Permission {
    public String name;
    public boolean granted;

    // don't ask again false: 用户拒绝并选择不再提示 true:用户拒绝但没有选择不再提示
    public boolean shouldShowRequestPermissionRationale;

    public Permission() {
    }

    public Permission(String name, boolean granted,
                      boolean shouldShowRequestPermissionRationale) {
        this.name = name;
        this.granted = granted;
        this.shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale;
    }
}
