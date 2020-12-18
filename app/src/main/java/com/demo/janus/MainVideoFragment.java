package com.demo.janus;

import android.view.View;
import android.widget.Button;

import com.demo.janus.capturer.OwtVideoCapturer;
import com.demo.janus.meet.MeetingClient;
import com.demo.janus.meet.MeetingOptions;
import com.demo.janus.meet.Resolution;
import com.demo.janus.renderer.TextureViewRenderer;
import com.demo.janus.stream.LocalStream;
import com.demo.janus.stream.MediaConstraints;
import com.demo.janus.util.LogUtil;
import org.webrtc.RendererCommon;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author before
 * @Date 2020/12/10
 * @desc
 */
public class MainVideoFragment extends BaseFragment {
    private static final String TAG = MainVideoFragment.class.getSimpleName();

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private TextureViewRenderer mainRenderer;
    private TextureViewRenderer mainSmallRender;
    private Button mQuitBtn;

    private LocalStream mLocalStream;
    private OwtVideoCapturer mCapturer;

    private boolean mIsAttach = false;

    private MainVideoListener mainVideoListener;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main_video;
    }

    @Override
    protected void initView(View view) {
        mainRenderer = view.findViewById(R.id.main_video);
        mainSmallRender = view.findViewById(R.id.main_small_video);
        mQuitBtn = view.findViewById(R.id.quit_btn);
        mQuitBtn.setOnClickListener(v -> leaveRoom());
        initRenderer(mainRenderer);
        initRenderer(mainSmallRender);
    }

    public void setMainVideoListener(MainVideoListener listener) {
        this.mainVideoListener = listener;
    }

    private void publish() {
        if (!mIsAttach) {
            mExecutor.execute(() -> {
                Resolution resolution = MeetingOptions.resolution;
                mCapturer = OwtVideoCapturer.create(resolution.width, resolution.height, 30, true,
                        true);
                mLocalStream = new LocalStream(mCapturer,
                        new MediaConstraints.AudioTrackConstraints());
                mLocalStream.attach(mainRenderer);
                MeetingClient.getInstance().publish(mLocalStream);
                mIsAttach = true;
            });
        }
    }

    private void leaveRoom() {
        stopPublish();
        stopSubscribe();
        MeetingClient.getInstance().leaveRoom();
    }

    private void stopPublish() {
        if (mLocalStream != null) {
            mLocalStream.detach(mainRenderer);
        }
        if (mCapturer != null) {
            mCapturer.stopCapture();
            mCapturer.dispose();
            mCapturer = null;
        }
        if (mLocalStream != null) {
            mLocalStream.dispose();
            mLocalStream = null;
        }
    }

    private void stopSubscribe() {
        if (mainVideoListener != null) {
            mainVideoListener.onLeaveRoom();
        }
    }

    @Override
    public void onResume() {
        publish();
//        if (mLocalStream != null) {
//            mLocalStream.attach(mainRenderer);
//        }
        super.onResume();
        LogUtil.d(TAG, "onResume");
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtil.d(TAG, "onStart");
    }

    @Override
    public void onPause() {
//        if (mLocalStream != null) {
//            mLocalStream.detach(mainRenderer);
//        }
        super.onPause();
        LogUtil.d(TAG, "onPause");
    }

    @Override
    public void onDestroyView() {
        mainRenderer.release();
        mainRenderer = null;
        mainSmallRender.release();
        mainSmallRender = null;
        super.onDestroyView();
        LogUtil.d(TAG, "onDestroyView");
    }


    public static MainVideoFragment getInstance() {
        MainVideoFragment mainVideoFragment = new MainVideoFragment();
        return mainVideoFragment;
    }

    private void initRenderer(TextureViewRenderer renderer) {
        renderer.init(MeetingClient.mRootEglBase.getEglBaseContext(), null);
        renderer.setMirror(true);
        renderer.setEnableHardwareScaler(true);
        renderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
    }
}
