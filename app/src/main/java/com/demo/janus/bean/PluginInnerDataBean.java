package com.demo.janus.bean;

import android.text.TextUtils;

import com.demo.janus.handle.VideoRoomMessageType;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public class PluginInnerDataBean implements Serializable {
    private String videoroom;
    private int room;
    private String started;
    private String description;
    private BigInteger id;
    private BigInteger leaving;
    private List<PublisherBean> publishers;

    public VideoRoomMessageType getVideoRoomMessageType() {
        if(TextUtils.isEmpty(videoroom)) return null;
        return VideoRoomMessageType.getVideoRoomMessageType(videoroom);
    }

    public String getVideoroom() {
        return videoroom;
    }

    public void setVideoroom(String videoroom) {
        this.videoroom = videoroom;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public String getStarted() {
        return started;
    }

    public void setStarted(String started) {
        this.started = started;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public List<PublisherBean> getPublishers() {
        return publishers;
    }

    public void setPublishers(List<PublisherBean> publishers) {
        this.publishers = publishers;
    }

    public BigInteger getLeaving() {
        return leaving;
    }

    public void setLeaving(BigInteger leaving) {
        this.leaving = leaving;
    }
}
