package com.demo.janus.bean;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public class EventResponseBean extends ResponseBean {
    private PluginDataBean plugindata;
    private JsepBean jsep;

    public PluginDataBean getPlugindata() {
        return plugindata;
    }

    public void setPlugindata(PluginDataBean plugindata) {
        this.plugindata = plugindata;
    }

    public JsepBean getJsep() {
        return jsep;
    }

    public void setJsep(JsepBean jsep) {
        this.jsep = jsep;
    }
}
