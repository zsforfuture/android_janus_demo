package com.demo.janus;

import android.Manifest;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.janus.bean.PublisherBean;
import com.demo.janus.capturer.OwtVideoCapturer;
import com.demo.janus.meet.MeetingClient;
import com.demo.janus.meet.MeetingOptions;
import com.demo.janus.permission.IListPermissionListener;
import com.demo.janus.permission.Permission;
import com.demo.janus.permission.PermissionManager;
import com.demo.janus.stream.LocalStream;
import com.demo.janus.stream.MediaConstraints;
import com.demo.janus.stream.SubscribeStream;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DemoActivity extends AppCompatActivity {

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private SurfaceViewRenderer localRenderer;
    private SurfaceViewRenderer remoteRenderer;
    private Button connectBtn;
    private Button publishBtn;
    private Button subscribeBtn;
    private Button unSubscribeBtn;

    //    private EglBase mRootEglBase;
    private MeetingClient meetingClient;

    private LocalStream mLocalStream;
    private OwtVideoCapturer mCapturer;
    private SubscribeStream subscribeStream;
    private PublisherBean user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        init();
        initView();
    }

    private void init() {
        meetingClient = MeetingClient.getInstance();
        meetingClient.reconnect();
    }


    private void initView() {
        localRenderer = findViewById(R.id.meet_local_render);
        initRenderer(localRenderer);
        remoteRenderer = findViewById(R.id.meet_remote_render);
        initRenderer(remoteRenderer);
        connectBtn = findViewById(R.id.connect);
        publishBtn = findViewById(R.id.publish);
        subscribeBtn = findViewById(R.id.subscribe);
        unSubscribeBtn = findViewById(R.id.unsubscribe);

        connectBtn.setOnClickListener(v -> {
            MeetingOptions meetingOptions = new MeetingOptions();
            MeetingOptions.userName = "我是zs";
            MeetingOptions.meetNum = 1234;
            MeetingOptions.audio = true;
            MeetingOptions.video = true;
            meetingClient.joinMeeting(meetingOptions);
        });

        publishBtn.setOnClickListener(v -> {
            PermissionManager.getInstance().requestPermissions(DemoActivity.this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, new IListPermissionListener() {
                        @Override
                        public void onGranted() {
                            mExecutor.execute(() -> {
                                mCapturer = OwtVideoCapturer.create(640, 480, 30, true,
                                        true);
                                mLocalStream = new LocalStream(mCapturer,
                                        new MediaConstraints.AudioTrackConstraints());
                                mLocalStream.attach(localRenderer);
                                meetingClient.publish(mLocalStream);
                            });
                        }

                        @Override
                        public void onRefused(List<Permission> permissions) {
                            Toast.makeText(DemoActivity.this, "请先授权权限", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        subscribeBtn.setOnClickListener(v -> {
            List<PublisherBean> users = meetingClient.getAllUsers();
            if (users.size() > 0) {
                user = users.get(0);
                subscribeStream = new SubscribeStream(String.valueOf(user.getId()), user.getDisplay());
                meetingClient.subscribe(subscribeStream, user, () -> {
                    subscribeStream.attach(remoteRenderer);
                });
            }
        });

        unSubscribeBtn.setOnClickListener(v -> {
            if (user != null && subscribeStream != null) {
                MeetingClient.getInstance().unSubscribe(user);
            }
        });
    }


    private void initRenderer(SurfaceViewRenderer renderer) {
        renderer.init(MeetingClient.mRootEglBase.getEglBaseContext(), null);
        renderer.setMirror(true);
        renderer.setEnableHardwareScaler(true);
        renderer.setZOrderMediaOverlay(true);
        renderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
    }
}