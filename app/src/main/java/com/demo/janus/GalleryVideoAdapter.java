package com.demo.janus;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.janus.bean.PublisherBean;
import com.demo.janus.meet.MeetingClient;
import com.demo.janus.renderer.TextureViewRenderer;
import com.demo.janus.stream.SubscribeStream;

import org.webrtc.RendererCommon;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author before
 * @Date 2020/12/10
 * @desc
 */
class GalleryVideoAdapter extends RecyclerView.Adapter<GalleryVideoAdapter.ItemVideoHolder> {
    private final List<PublisherBean> mUsers;
    private final ConcurrentHashMap<PublisherBean, OnSubscribeListener> mUnSubscribeListenerMap;
    private final LayoutInflater mInflater;
    private float mItemHeight = 0f;
    private boolean showStats = true;
    private final Context context;

    public GalleryVideoAdapter(Context context, List<PublisherBean> mUsers) {
        this.context = context;
        this.mUsers = mUsers;
        mInflater = LayoutInflater.from(context);
        mUnSubscribeListenerMap = new ConcurrentHashMap<>();
    }

    public void setShowStats(boolean stats) {
        this.showStats = stats;
    }

    @NonNull
    @Override
    public ItemVideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemVideoHolder(mInflater.inflate(R.layout.item_user_video, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemVideoHolder holder, int position) {
        holder.bindData(mUsers.get(position));
    }

    public void setItemHeight(float height) {
        this.mItemHeight = height;
    }

    public void notifyOrientationChange(float height) {
        setItemHeight(height);
        for (PublisherBean user : mUnSubscribeListenerMap.keySet()) {
            if (mUnSubscribeListenerMap.get(user) != null) {
                mUnSubscribeListenerMap.get(user).onOrientationChange();
            }
        }
    }

    public void notifyUnSubscribe() {
        for (PublisherBean user : mUnSubscribeListenerMap.keySet()) {
            if (mUnSubscribeListenerMap.get(user) != null) {
                mUnSubscribeListenerMap.get(user).onUnSubscribe(user);
            }
        }
    }

    public void notifyUpdateStats() {
        for (PublisherBean user : mUnSubscribeListenerMap.keySet()) {
            if (mUnSubscribeListenerMap.get(user) != null) {
                mUnSubscribeListenerMap.get(user).onUpdateStats(user);
            }
        }
    }

    public void notifyDestroy() {
        for (PublisherBean user : mUnSubscribeListenerMap.keySet()) {
            if (mUnSubscribeListenerMap.get(user) != null) {
                mUnSubscribeListenerMap.get(user).onDestroy();
                removeUnSubscribeListener(user);
            }
        }
    }

    public void addUnSubscribeListener(PublisherBean user, OnSubscribeListener listener) {
        if (mUnSubscribeListenerMap.get(user) == null) {
            mUnSubscribeListenerMap.put(user, listener);
        }
    }

    public void removeUnSubscribeListener(PublisherBean user) {
        if (mUnSubscribeListenerMap.get(user) != null) {
            mUnSubscribeListenerMap.remove(user);
        }
    }

    public interface OnSubscribeListener {
        void onOrientationChange();

        void onUnSubscribe(PublisherBean user);

        void onUpdateStats(PublisherBean user);

        void onDestroy();
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ItemVideoHolder extends RecyclerView.ViewHolder implements OnSubscribeListener {
        private TextureViewRenderer renderer;
        private final TextView userNameTv;
        private final TextView statsTv;
        private SubscribeStream subscribeStream;
        private StatsReportUtil statsReportUtil;

        public ItemVideoHolder(@NonNull View itemView) {
            super(itemView);
            renderer = itemView.findViewById(R.id.item_video);
            userNameTv = itemView.findViewById(R.id.user_name);
            statsTv = itemView.findViewById(R.id.video_status);
            renderer.init(MeetingClient.mRootEglBase.getEglBaseContext(), null);
            renderer.setMirror(true);
            renderer.setEnableHardwareScaler(true);
            renderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        }

        public void bindData(PublisherBean user) {
            setLayoutParams();
            MeetingClient.getInstance().unSubscribe(user);
            addUnSubscribeListener(user, this);
            userNameTv.setText(user.getDisplay());
            subscribeStream = new SubscribeStream(String.valueOf(user.getId()), user.getDisplay());
            MeetingClient.getInstance().subscribe(subscribeStream, user, () -> {
                        subscribeStream.attach(renderer);
                    }
            );
        }

        @Override
        public void onOrientationChange() {
            setLayoutParams();
        }

        private void setLayoutParams() {
            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            params.height = (int) mItemHeight;
            itemView.setLayoutParams(params);
            itemView.requestLayout();
        }

        @Override
        public void onUpdateStats(PublisherBean user) {
            if (showStats) {
                MeetingClient.getInstance().getStats(user, rtcStatsReport -> {
                    if (statsReportUtil == null) {
                        statsReportUtil = new StatsReportUtil();
                    }
                    String txt = statsReportUtil.getStatsReport(rtcStatsReport);
                    ((Activity) context).runOnUiThread(() -> {
                        statsTv.setText(txt);
                    });
                });
            }
        }

        @Override
        public void onUnSubscribe(PublisherBean user) {
            MeetingClient.getInstance().unSubscribe(user);
            if (subscribeStream != null && renderer != null) {
                subscribeStream.detach(renderer);
            }
            removeUnSubscribeListener(user);
        }

        @Override
        public void onDestroy() {
            if (renderer != null) {
                renderer.release();
                renderer = null;
            }
        }
    }
}
