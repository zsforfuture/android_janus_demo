package com.demo.janus.bean;

import java.io.Serializable;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public class PluginDataBean implements Serializable {
    private String plugin;
    private PluginInnerDataBean data;

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public PluginInnerDataBean getData() {
        return data;
    }

    public void setData(PluginInnerDataBean data) {
        this.data = data;
    }
}
