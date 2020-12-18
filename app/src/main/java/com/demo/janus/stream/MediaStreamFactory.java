/*
 * Copyright (C) 2018 Intel Corporation
 * SPDX-License-Identifier: Apache-2.0
 */
package com.demo.janus.stream;

import com.demo.janus.capturer.VideoCapturer;
import com.demo.janus.meet.ContextInitialization;
import com.demo.janus.meet.PCFactoryProxy;

import org.webrtc.AudioSource;
import org.webrtc.MediaStream;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.UUID;

import static com.demo.janus.stream.CheckCondition.DCHECK;
import static com.demo.janus.stream.CheckCondition.RCHECK;
import static com.demo.janus.meet.ContextInitialization.localContext;


final class MediaStreamFactory {

    private static MediaStreamFactory instance;
    private final HashMap<String, VideoSource> unsharedVideoSources = new HashMap<>();
    private AudioSource sharedAudioSource;
    private int audioSourceRef = 0;

    private MediaStreamFactory() {
    }

    synchronized static MediaStreamFactory instance() {
        if (instance == null) {
            instance = new MediaStreamFactory();
        }
        return instance;
    }

    MediaStream createMediaStream(VideoCapturer videoCapturer,
                                  MediaConstraints.AudioTrackConstraints audioMediaConstraints) {
        RCHECK(videoCapturer != null || audioMediaConstraints != null);

        String label = UUID.randomUUID().toString();
        MediaStream mediaStream = PCFactoryProxy.instance().createLocalMediaStream(label);

        if (videoCapturer != null) {
            VideoSource videoSource = PCFactoryProxy.instance().createVideoSource(
                    videoCapturer.isScreencast());
            SurfaceTextureHelper helper = SurfaceTextureHelper.create("CT", localContext);
            videoCapturer.initialize(helper, ContextInitialization.context,
                    videoSource.getCapturerObserver());
            videoCapturer.startCapture(videoCapturer.getWidth(),
                    videoCapturer.getHeight(),
                    videoCapturer.getFps());
            VideoTrack videoTrack = PCFactoryProxy.instance().createVideoTrack(label + "v0",
                    videoSource);
            videoTrack.setEnabled(true);
            mediaStream.addTrack(videoTrack);
            unsharedVideoSources.put(label, videoSource);
        }

        if (audioMediaConstraints != null) {
            if (sharedAudioSource == null) {
                sharedAudioSource = PCFactoryProxy.instance().createAudioSource(
                        audioMediaConstraints.convertToWebRTCConstraints());
            }
            audioSourceRef++;
            mediaStream.addTrack(
                    PCFactoryProxy.instance().createAudioTrack(label + "a0", sharedAudioSource));
        }

        return mediaStream;
    }

    void onAudioSourceRelease() {
        DCHECK(audioSourceRef > 0);
        if (--audioSourceRef == 0) {
            sharedAudioSource.dispose();
            sharedAudioSource = null;
        }
    }

    void onVideoSourceRelease(String label) {
        DCHECK(unsharedVideoSources.containsKey(label));
        VideoSource videoSource = unsharedVideoSources.get(label);
        unsharedVideoSources.remove(label);
        videoSource.dispose();
    }

}
