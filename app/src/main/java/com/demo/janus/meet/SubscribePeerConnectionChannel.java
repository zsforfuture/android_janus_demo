package com.demo.janus.meet;

import com.demo.janus.handle.IHandle;
import com.demo.janus.handle.imp.SubscribeHandle;
import com.demo.janus.stream.SubscribeStream;
import com.demo.janus.util.LogUtil;

import org.webrtc.PeerConnection;
import org.webrtc.RTCStatsCollectorCallback;

import static com.demo.janus.constant.Constant.SERVER_TAG;

/**
 * @Author before
 * @Date 2020/12/10
 * @desc
 */
public class SubscribePeerConnectionChannel extends PeerConnectionChannelV3 {


    public SubscribePeerConnectionChannel(PeerConnection.RTCConfiguration configuration, IHandle handle) {
        super(configuration, handle);
    }

    public void subscribe(SubscribeStream subscribeStream) {
        this.stream = subscribeStream;
    }

    public void getConnectionStats(RTCStatsCollectorCallback callback) {
        LogUtil.d(SERVER_TAG, "SubscribePeerConnectionChannel----->getConnectionStats");
        pcExecutor.execute(() -> {
            peerConnection.getStats(callback);
        });
    }

    @Override
    public void onSetSuccess() {
        super.onSetSuccess();
        LogUtil.d(SERVER_TAG, "SubscribePeerConnectionChannel --> onSetSuccess");
        if (sdp == null) {
            createAnswer();
        }
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        LogUtil.d(SERVER_TAG, "SubscribePeerConnectionChannel --> onIceConnectionChange---->" + iceConnectionState);
        if (iceConnectionState == PeerConnection.IceConnectionState.CONNECTED) {
            if (handle != null) {
                ((SubscribeHandle) handle).onSubscribeStreamReady((SubscribeStream) stream);
            }
        }
    }
}
