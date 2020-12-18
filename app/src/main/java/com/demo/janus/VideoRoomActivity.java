package com.demo.janus;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.demo.janus.bean.PublisherBean;
import com.demo.janus.constant.Constant;
import com.demo.janus.meet.MeetingClient;
import com.demo.janus.meet.MeetingObserver;
import com.demo.janus.meet.MeetingOptions;
import com.demo.janus.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.demo.janus.constant.Constant.PAGE_COUNT;

public class VideoRoomActivity extends AppCompatActivity implements MeetingObserver, MainVideoListener {

    private RelativeLayout mContentView;
    private ViewPager2 mViewPager;
    private float mViewPagerHeight = 0f;
    private VideoAdapter mAdapter;
    private final List<PublisherBean> mUsers = new ArrayList<>();
    private boolean isInitView;
    private ConcurrentHashMap<Integer, List<PublisherBean>> mPageMap;
    private final List<Fragment> mFragments = new ArrayList<>(4);
    private MainVideoFragment mainVideoFragment;
    private int mCurrentPage = 0;

    public int mOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_room);
        MeetingClient.getInstance().setMeetingObserver(this);
        MeetingClient.getInstance().reconnect();
        mPageMap = new ConcurrentHashMap<>();
        mOrientation = getResources().getConfiguration().orientation;
    }

    private void initView() {
        mContentView = findViewById(R.id.video_content);
        mViewPager = findViewById(R.id.viewpager);
        initViewPager();
    }

    private void showMainVideo() {
        mainVideoFragment = MainVideoFragment.getInstance();
        mainVideoFragment.setMainVideoListener(this);
        mFragments.add(mainVideoFragment);
        mAdapter.notifyDataSetChanged();
    }

    private void addGalleryVideoFragment(List<PublisherBean> users) {
        GalleryVideoFragment fragment = GalleryVideoFragment.getInstance();
        fragment.setItemViewHeight(mViewPagerHeight / 2);
        fragment.setUser(users);
        mFragments.add(fragment);
    }

    private synchronized void initUser(List<PublisherBean> users) {
        mUsers.addAll(users);
        int page = getMaxPage();
        for (int i = 0; i < page; i++) {
            int end = Math.min((i + 1) * PAGE_COUNT, mUsers.size());
            List<PublisherBean> list = new ArrayList<>(mUsers.subList(i * PAGE_COUNT, end));
            mPageMap.put(i + 1, list);
            addGalleryVideoFragment(list);
        }

        if (page > 1) {
            mAdapter.notifyItemChanged(1, mFragments.size());
        }
    }

    private synchronized void addUser(List<PublisherBean> users) {
        int beforePage = getMaxPage();
        mUsers.addAll(users);
        List<PublisherBean> curs = mPageMap.get(beforePage);
        if (curs != null) {
            int diffSize = PAGE_COUNT - curs.size();
            if (diffSize > 0) {
                GalleryVideoFragment fragment = (GalleryVideoFragment) mFragments.get(beforePage);
                if (users.size() <= diffSize) {
                    curs.addAll(users);
                    mPageMap.put(beforePage, curs);
                    fragment.addUser(users);
                    return;
                } else {
                    List<PublisherBean> temp = new ArrayList<>(users.subList(0, diffSize));
                    curs.addAll(temp);
                    users.removeAll(temp);
                    fragment.addUser(temp);
                    mPageMap.put(beforePage, curs);
                }
            }

            int tempPage = users.size() / PAGE_COUNT + (users.size() % PAGE_COUNT > 0 ? 1 : 0);
            for (int i = 0; i < tempPage; i++) {
                beforePage++;
                int end = Math.min((i + 1) * PAGE_COUNT, users.size());
                List<PublisherBean> list = new ArrayList<>(users.subList(i * PAGE_COUNT, end));
                mPageMap.put(beforePage, list);
                addGalleryVideoFragment(list);
            }
        }
        mAdapter.notifyItemChanged(1, mFragments.size());
    }

    public synchronized void removeUser(List<PublisherBean> users) {
        for (PublisherBean user : users) {
            removeUser(user);
        }

    }

    private synchronized void removeUser(PublisherBean user) {
        int maxPage = getMaxPage();
        mUsers.remove(user);
        int inPage = -1;
        List<PublisherBean> inPageList = new ArrayList<>();
        for (Integer page : mPageMap.keySet()) {
            if (mPageMap.get(page) != null && mPageMap.get(page).contains(user)) {
                inPage = page;
                inPageList.addAll(mPageMap.get(page));
                break;
            }
        }

        GalleryVideoFragment lastFragment = getLastFragment();
        if (lastFragment == null) return;

        if (inPage == maxPage) {
            // 如果在最后一页,不用替换，直接remove
            if (inPageList.size() == 1) {
                mFragments.remove(mFragments.size() - 1);
                mPageMap.remove(inPage);
            } else {
                lastFragment.removeUser(user);
                inPageList.remove(user);
                mPageMap.put(inPage, inPageList);
            }
        } else if (inPage < maxPage) {
            List<PublisherBean> lastMapUsers = mPageMap.get(maxPage);
            if (lastMapUsers != null || lastMapUsers.size() <= 0) return;
            PublisherBean lastUser = lastMapUsers.get(lastMapUsers.size() - 1);
            lastMapUsers.remove(lastUser);
            // 处理最后一个用户的逻辑
            if (lastMapUsers.size() == 0) {
                // 移除掉最后一个
                mFragments.remove(mFragments.size() - 1);
                mPageMap.remove(maxPage);
            } else {
                lastFragment.removeUser(lastUser);
                inPageList.remove(lastUser);
                mPageMap.put(maxPage, inPageList);
            }

            int pos = inPageList.indexOf(user);
            GalleryVideoFragment inFragment = getInPageFragment(inPage);
            if (inFragment == null) return;
            inPageList.remove(user);
            inPageList.add(pos, lastUser);
            inFragment.replaceUser(pos, lastUser);
        }
    }

    private GalleryVideoFragment getInPageFragment(int page) {
        if (page < mFragments.size() && mFragments.get(page) instanceof GalleryVideoFragment) {
            return (GalleryVideoFragment) mFragments.get(page);
        }
        return null;
    }


    private GalleryVideoFragment getLastFragment() {
        if (mFragments.size() <= 1) return null;

        Fragment fragment = mFragments.get(mFragments.size() - 1);
        if (fragment instanceof GalleryVideoFragment) {
            return (GalleryVideoFragment) fragment;
        }
        return null;
    }

    private List<PublisherBean> getLastMap() {
        int maxPage = getMaxPage();
        return mPageMap.get(maxPage);
    }


    private int getMaxPage() {
        return mUsers.size() / PAGE_COUNT + (mUsers.size() % PAGE_COUNT > 0 ? 1 : 0);
    }

    private void initViewPager() {
//        mViewPagerHeight = mViewPager.getMeasuredHeight();
        mViewPagerHeight = mContentView.getMeasuredHeight();
        mViewPager.setOffscreenPageLimit(1);
        mAdapter = new VideoAdapter(this, mFragments);
        mViewPager.setAdapter(mAdapter);
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mCurrentPage = position;
                LogUtil.e(Constant.MEETING_TAG, "mCurrentPage--->" + mCurrentPage);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    @Override
    public void onLeaveRoom() {
        if (mFragments.get(mCurrentPage) != null && (mFragments.get(mCurrentPage) instanceof GalleryVideoFragment)) {
            ((GalleryVideoFragment) mFragments.get(mCurrentPage)).unSubscribe();
        }
    }

    @Override
    public void onConnected() {
        MeetingOptions meetingOptions = new MeetingOptions();
        meetingOptions.meetNum = 1234;
        meetingOptions.userName = "不行就尴尬了";
        meetingOptions.video = true;
        meetingOptions.audio = true;
        MeetingClient.getInstance().joinMeeting(meetingOptions);
    }

    @Override
    public void onJoinRoom() {
        showMainVideo();
    }

    @Override
    public void onUserInRoom(List<PublisherBean> users) {
        initUser(users);
    }

    @Override
    public void onUserJoin(List<PublisherBean> users) {
        LogUtil.d(Constant.SERVER_TAG, "activity onUserJoin----> " + users.size());
        addUser(users);
    }

    @Override
    public void onUserLeave(List<PublisherBean> users) {
        LogUtil.d(Constant.SERVER_TAG, "activity onUserLeave----> " + users.size());
        removeUser(users);
    }

    @Override
    public void onDisconnect() {
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !isInitView) {
            initView();
            isInitView = true;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != mOrientation) {
            mOrientation = newConfig.orientation;
            mViewPager.requestLayout();
            mViewPager.post(() -> {
                mViewPagerHeight = mViewPager.getMeasuredHeight();
                for (Fragment fragment : mFragments) {
                    if (fragment instanceof GalleryVideoFragment) {
                        ((GalleryVideoFragment) fragment).setItemViewHeight(mViewPagerHeight / 2);
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        // 禁用系统返回按键
        //super.onBackPressed();
    }
}