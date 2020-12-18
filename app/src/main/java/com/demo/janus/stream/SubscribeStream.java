package com.demo.janus.stream;

import org.webrtc.MediaStream;

/**
 * @Author before
 * @Date 2020/12/10
 * @desc
 */
public class SubscribeStream extends RemoteStream {

    public SubscribeStream(String id, String origin) {
        super(id, origin);
    }


    public MediaStream getMediaStream() {
        return mediaStream;
    }

    public void setMediaStream(MediaStream mediaStream) {
        this.mediaStream = mediaStream;
    }


    public void onEnded() {
        triggerEndedEvent();
    }
}
