package com.demo.janus.meet;

import com.demo.janus.handle.IHandle;
import com.demo.janus.stream.Stream;
import com.demo.janus.stream.SubscribeStream;
import com.demo.janus.util.LogUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpSender;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.demo.janus.constant.Constant.SERVER_TAG;

/**
 * @Author before
 * @Date 2020/12/9
 * @desc
 */
public abstract class PeerConnectionChannelV3 implements
        PeerConnection.Observer, SdpObserver, DataChannel.Observer {


    // <MediaStream id, RtpSender>
    protected ConcurrentHashMap<String, RtpSender> videoRtpSenders, audioRtpSenders;

    protected final ExecutorService callbackExecutor = Executors.newSingleThreadExecutor();
    protected final ExecutorService pcExecutor = Executors.newSingleThreadExecutor();
    private final Object remoteIceLock = new Object();
    private final Object disposeLock = new Object();

    protected MediaConstraints sdpConstraints;
    protected PeerConnection peerConnection;
    protected IHandle handle;

    protected SessionDescription sdp;

    private boolean disposed = false;

    protected Stream stream;

    public PeerConnectionChannelV3(PeerConnection.RTCConfiguration configuration, IHandle handle) {
        this.handle = handle;
        videoRtpSenders = new ConcurrentHashMap<>();
        audioRtpSenders = new ConcurrentHashMap<>();
        sdpConstraints = new MediaConstraints();
        sdpConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        sdpConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        peerConnection = PCFactoryProxy.instance().createPeerConnection(configuration, this);
    }


    public void createOffer() {
        pcExecutor.execute(() -> {
            if (disposed()) {
                return;
            }
            LogUtil.d(SERVER_TAG, "---->create offer");
            peerConnection.createOffer(PeerConnectionChannelV3.this, sdpConstraints);
        });
    }

    public void createAnswer() {
        pcExecutor.execute(() -> {
            if (disposed()) {
                return;
            }
            LogUtil.d(SERVER_TAG, "---->create answer");
            peerConnection.createAnswer(PeerConnectionChannelV3.this, sdpConstraints);
        });
    }

    public void setRemoteDescription(SessionDescription sessionDescription) {
        LogUtil.d(SERVER_TAG, "--> setRemoteDescription");

        pcExecutor.execute(() -> {
            if (disposed()) {
                return;
            }
            peerConnection.setRemoteDescription(PeerConnectionChannelV3.this, sessionDescription);
        });
    }

    protected void dispose() {
        pcExecutor.execute(() -> {
            synchronized (disposeLock) {
                disposed = true;
                if (peerConnection != null) {
                    peerConnection.dispose();
                }
                peerConnection = null;
            }
        });
    }

    public boolean disposed() {
        synchronized (disposeLock) {
            return disposed;
        }
    }

    /**
     * -----------------------------------SdpObserver------------------------------------
     */
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        LogUtil.d(SERVER_TAG, "onCreateSuccess---->" + sessionDescription.description);
        if (sdp == null) {
            sdp = sessionDescription;
            pcExecutor.execute(() -> {
                if (disposed) {
                    return;
                }
                peerConnection.setLocalDescription(PeerConnectionChannelV3.this, sessionDescription);
            });
        }

        callbackExecutor.execute(() -> {
            if (disposed) {
                return;
            }
            SessionDescription sdp =
                    new SessionDescription(sessionDescription.type,
                            sessionDescription.description.replaceAll(
                                    "a=ice-options:google-ice\r\n", ""));
            try {
                JSONObject sdpObj = new JSONObject();
                sdpObj.put("type", sdp.type.toString().toLowerCase(Locale.US));
                sdpObj.put("sdp", sdp.description);
                handle.sendLocalDescription(sdpObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public void onSetSuccess() {
        LogUtil.d(SERVER_TAG, "PeerConnectionChannelV3 --> onSetSuccess");
    }

    @Override
    public void onCreateFailure(String s) {
        LogUtil.d(SERVER_TAG, "onCreateFailure---->" + s);
    }

    @Override
    public void onSetFailure(String s) {
        LogUtil.d(SERVER_TAG, "onSetFailure---->" + s);
    }


    /**
     * -----------------------------------PeerConnection.Observer------------------------------------
     */
    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        LogUtil.d(SERVER_TAG, "onSignalingChange---->" + signalingState);
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        LogUtil.d(SERVER_TAG, "onIceConnectionChange---->" + iceConnectionState);
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        LogUtil.d(SERVER_TAG, "onIceConnectionReceivingChange---->" + b);
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        LogUtil.d(SERVER_TAG, "onIceGatheringChange---->" + iceGatheringState);
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {
        LogUtil.d(SERVER_TAG, "---->onIceCandidate");
        try {
            JSONObject candidateObj = new JSONObject();
            candidateObj.put("sdpMLineIndex", candidate.sdpMLineIndex);
            candidateObj.put("sdpMid", candidate.sdpMid);
            candidateObj.put("candidate", candidate.sdp);
            handle.sendIceCandidate(candidateObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        LogUtil.d(SERVER_TAG, "PeerConnectionChannelV3 -> onIceCandidatesRemoved");
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        LogUtil.d(SERVER_TAG, "PeerConnectionChannelV3 -> onAddStream---->" + mediaStream.getId());
        callbackExecutor.execute(() -> {
            ((SubscribeStream) stream).setMediaStream(mediaStream);
        });
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        LogUtil.d(SERVER_TAG, "PeerConnectionChannelV3 -> onRemoveStream---->" + mediaStream.getId());
        callbackExecutor.execute(() -> ((SubscribeStream) stream).onEnded());
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        LogUtil.d(SERVER_TAG, "PeerConnectionChannelV3 -> onDataChannel");
    }

    @Override
    public void onRenegotiationNeeded() {
        LogUtil.d(SERVER_TAG, "PeerConnectionChannelV3 -> onRenegotiationNeeded");
    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

    }

    /**
     * -----------------------------------DataChannel.Observer------------------------------------
     */
    @Override
    public void onBufferedAmountChange(long l) {

    }

    @Override
    public void onStateChange() {

    }

    @Override
    public void onMessage(DataChannel.Buffer buffer) {

    }

}
