package com.demo.janus;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.janus.bean.PublisherBean;
import com.demo.janus.meet.MeetingOptions;
import com.demo.janus.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.demo.janus.constant.Constant.PAGE_COUNT;

/**
 * @Author before
 * @Date 2020/12/10
 * @desc
 */
public class GalleryVideoFragment extends BaseFragment {
    private static final String TAG = GalleryVideoFragment.class.getSimpleName();
    private static final int COUNT = (int) Math.sqrt(PAGE_COUNT);
    private RecyclerView mListView;
    private GalleryVideoAdapter mAdapter;
    private final List<PublisherBean> mUsers = new ArrayList<>(PAGE_COUNT);
    private float mViewHeight = 0f;
    private final boolean mShowStats = MeetingOptions.showStats;
    private Timer mStatsTimer;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_gallery_video;
    }

    @Override
    protected void initView(View view) {
        mListView = view.findViewById(R.id.gallery_list);
    }

    @Override
    protected void initData(Bundle bundle) {

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void unSubscribe() {
        if (mAdapter != null) {
            mAdapter.notifyUnSubscribe();
        }
    }

    private void updateListView() {
        if (mAdapter == null) {
            mAdapter = new GalleryVideoAdapter(getActivity(), mUsers);
            mAdapter.setItemHeight(mViewHeight);
            mAdapter.setShowStats(mShowStats);
            mListView.setLayoutManager(new GridLayoutManager(getContext(), COUNT));
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void startTimer() {
        if (mShowStats) {
            stopTimer();
            mStatsTimer = new Timer();
            mStatsTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mAdapter != null) {
                        mAdapter.notifyUpdateStats();
                    }
                }
            }, 0, 5000);
        }
    }

    private void stopTimer() {
        if (mStatsTimer != null) {
            mStatsTimer.cancel();
            mStatsTimer = null;
        }
    }

    public List<PublisherBean> getUsers() {
        return mUsers;
    }

    public void setItemViewHeight(float height) {
        this.mViewHeight = height;
        if (mAdapter != null) {
            if (isResumed()) {
                mAdapter.notifyOrientationChange(mViewHeight);
            } else {
                mAdapter.setItemHeight(mViewHeight);
            }
        }
    }

    public void setUser(List<PublisherBean> users) {
        mUsers.clear();
        mUsers.addAll(users);
    }

    public void addUser(List<PublisherBean> users) {
        mUsers.addAll(users);
        if (isResumed()) {
            mAdapter.notifyItemChanged(mUsers.size() - users.size(), mUsers.size());
        }
    }

    public void removeUser(PublisherBean user) {
        int pos = mUsers.indexOf(user);
        mUsers.remove(user);
        if (isResumed()) {
            mAdapter.notifyItemRangeRemoved(pos, pos + 1);
        }
    }

    public void replaceUser(int pos, PublisherBean user) {
        mUsers.remove(pos);
        mUsers.add(pos, user);
        if (isResumed()) {
            mAdapter.notifyItemRangeRemoved(pos, pos + 1);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtil.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        updateListView();
        super.onResume();
        startTimer();
        LogUtil.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        if (mAdapter != null) {
            mAdapter.notifyUnSubscribe();
        }
        super.onPause();
        startTimer();
        LogUtil.d(TAG, "onPause");
    }

    @Override
    public void onDestroyView() {
        if (mAdapter != null) {
            mAdapter.notifyDestroy();
        }
        stopTimer();
        super.onDestroyView();
        LogUtil.d(TAG, "onDestroyView");
    }

    public static GalleryVideoFragment getInstance() {
        GalleryVideoFragment galleryVideoFragment = new GalleryVideoFragment();
        return galleryVideoFragment;
    }

}
