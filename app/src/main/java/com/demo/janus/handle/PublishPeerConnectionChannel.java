package com.demo.janus.handle;

import com.demo.janus.meet.PCFactoryProxy;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;

/**
 * @Author before
 * @Date 2020/12/8
 * @desc
 */
public class PublishPeerConnectionChannel implements PeerConnection.Observer {

    private MediaConstraints sdpConstraints;
    private PeerConnection peerConnection;
    private IPublishHandle publishHandle;

    public PublishPeerConnectionChannel(boolean video, boolean audio,
                                        PeerConnection.RTCConfiguration configuration,
                                        IPublishHandle publishHandle) {
        this.publishHandle = publishHandle;
        sdpConstraints = new MediaConstraints();
        sdpConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", String.valueOf(audio)));
        sdpConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", String.valueOf(video)));
        peerConnection = PCFactoryProxy.instance().createPeerConnection(configuration, this);
    }


    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {

    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {

    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {

    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {

    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {

    }

    @Override
    public void onRenegotiationNeeded() {

    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

    }

    @Override
    public void onConnectionChange(PeerConnection.PeerConnectionState newState) {

    }

    @Override
    public void onTrack(RtpTransceiver transceiver) {

    }

    @Override
    public void onAddStream(MediaStream mediaStream) {

    }
}
