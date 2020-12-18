package com.demo.janus.meet;

import com.demo.janus.constant.Constant;
import com.demo.janus.handle.IHandle;
import com.demo.janus.stream.LocalStream;
import com.demo.janus.util.LogUtil;

import org.webrtc.AudioTrack;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpSender;
import org.webrtc.VideoTrack;

import java.util.ArrayList;

/**
 * @Author before
 * @Date 2020/12/8
 * @desc
 */
public class PublishPeerConnectionChannel extends PeerConnectionChannelV3 {

    public PublishPeerConnectionChannel(PeerConnection.RTCConfiguration configuration, IHandle handle) {
        super(configuration, handle);
    }


    public void publish(LocalStream localStream) {
        LogUtil.d(Constant.SERVER_TAG, "------>publish");
        stream = localStream;
        addStream(localStream.mediaStream);
        createOffer();
    }

    protected void addStream(final MediaStream mediaStream) {
        pcExecutor.execute(() -> {
            if (disposed()) {
                return;
            }
            ArrayList<String> streamIds = new ArrayList<>();
            streamIds.add(mediaStream.getId());
            for (AudioTrack audioTrack : mediaStream.audioTracks) {
                RtpSender audioSender = peerConnection.addTrack(audioTrack, streamIds);
                audioRtpSenders.put(mediaStream.getId(), audioSender);
            }
            for (VideoTrack videoTrack : mediaStream.videoTracks) {
                RtpSender videoSender = peerConnection.addTrack(videoTrack, streamIds);
                videoRtpSenders.put(mediaStream.getId(), videoSender);
            }
        });
    }
}
