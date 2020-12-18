/*
 * Copyright (C) 2018 Intel Corporation
 * SPDX-License-Identifier: Apache-2.0
 */
package com.demo.janus.capturer;

import com.demo.janus.stream.Stream;

import org.webrtc.FileVideoCapturer;

import java.io.IOException;


public class OwtFileVideoCapturer extends FileVideoCapturer implements VideoCapturer {

    public OwtFileVideoCapturer(String inputFile) throws IOException {
        super(inputFile);
    }

    @Override
    public int getWidth() {
        // ignored
        return 0;
    }

    @Override
    public int getHeight() {
        // ignored
        return 0;
    }

    @Override
    public int getFps() {
        return 30;
    }

    @Override
    public Stream.StreamSourceInfo.VideoSourceInfo getVideoSource() {
        return Stream.StreamSourceInfo.VideoSourceInfo.RAW_FILE;
    }
}
