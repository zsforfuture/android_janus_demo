package com.demo.janus.meet;

import android.content.Context;

import com.demo.janus.bean.PublisherBean;
import com.demo.janus.stream.LocalStream;
import com.demo.janus.stream.SubscribeStream;
import com.demo.janus.util.LogUtil;
import org.webrtc.EglBase;
import org.webrtc.PeerConnection;
import org.webrtc.RTCStatsCollectorCallback;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.List;

import static org.webrtc.PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;

/**
 * @Author before
 * @Date 2020/12/7
 * @desc
 */
public class MeetingClient {

    private static volatile MeetingClient meetingClient;
    private final MeetingService meetingService;
    public static EglBase mRootEglBase;

    private MeetingClient() {
        LogUtil.init(true);
        PeerConnection.RTCConfiguration rtcConfiguration = new PeerConnection.RTCConfiguration(
                new ArrayList<>());
        rtcConfiguration.enableDtlsSrtp = true;
        rtcConfiguration.continualGatheringPolicy = GATHER_CONTINUALLY;
        ConferenceClientConfiguration configuration
                = ConferenceClientConfiguration.builder()
                .setHostnameVerifier(HttpUtils.hostnameVerifier)
                .setSSLContext(HttpUtils.sslContext)
                .setRTCConfiguration(rtcConfiguration)
                .build();
        meetingService = new MeetingService(configuration);
    }

    public static MeetingClient getInstance() {
        if (meetingClient == null) {
            synchronized (MeetingClient.class) {
                if (meetingClient == null) {
                    meetingClient = new MeetingClient();
                }
            }
        }
        return meetingClient;
    }

    public static void init(Context context) {
        mRootEglBase = EglBase.create();
        ContextInitialization.create()
                .setApplicationContext(context)
                .addIgnoreNetworkType(ContextInitialization.NetworkType.LOOPBACK)
                .setVideoHardwareAccelerationOptions(
                        mRootEglBase.getEglBaseContext(),
                        mRootEglBase.getEglBaseContext())
                .initialize();
    }

    public void reconnect() {
        meetingService.reconnect();
    }

    public void joinMeeting(MeetingOptions options) {
        meetingService.joinRoom(options);
    }

    public void publish(LocalStream stream) {
        meetingService.publish(stream);
    }

    public void subscribe(SubscribeStream stream, PublisherBean user, ISubscribeAck ack) {
        meetingService.subscribe(stream, user, ack);
    }

    public void subscribe(SurfaceViewRenderer renderer, SubscribeStream stream, PublisherBean user) {
        meetingService.subscribe(stream, user, () -> stream.attach(renderer));
    }

    public void getStats(PublisherBean user, RTCStatsCollectorCallback callback) {
        meetingService.getStats(user, callback);
    }

    public void unSubscribe(PublisherBean user) {
        meetingService.unSubscribe(user);
    }

    public SubscribeStream getSubscribeStream(PublisherBean user) {
        return null;
    }

    public void leaveRoom() {
        meetingService.leaveRoom();
    }

    public List<PublisherBean> getAllUsers() {
        return meetingService.getAllUsers();
    }

    public void setMeetingObserver(MeetingObserver meetingObserver) {
        meetingService.setMeetingObserver(meetingObserver);
    }
}
