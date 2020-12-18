package com.demo.janus.bean;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public class PublisherBean implements Serializable {
    private BigInteger id;
    private String display;
    private String audio_codec;
    private String video_codec;
    private boolean talking;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getAudio_codec() {
        return audio_codec;
    }

    public void setAudio_codec(String audio_codec) {
        this.audio_codec = audio_codec;
    }

    public String getVideo_codec() {
        return video_codec;
    }

    public void setVideo_codec(String video_codec) {
        this.video_codec = video_codec;
    }

    public boolean isTalking() {
        return talking;
    }

    public void setTalking(boolean talking) {
        this.talking = talking;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublisherBean that = (PublisherBean) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
