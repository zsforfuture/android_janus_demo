package com.demo.janus.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.demo.janus.R;

/**
 * @Author before
 * @Date 2020/6/11
 * @desc
 */
public abstract class BaseDialog extends DialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isFullScreen()) {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.common_DialogFullScreen);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    protected abstract int getLayoutId();

    protected abstract void initView(View view);

    /**
     * 是否去掉半透明阴影
     *
     * @return
     */
    protected boolean isTransparent() {
        return false;
    }

    /**
     * 是否全屏显示去掉dialog的padding
     *
     * @return
     */
    protected boolean isFullScreen() {
        return false;
    }

    /**
     * 设置动画
     *
     * @return 返回styleId
     */
    protected int getAnimation() {
        return -1;
    }

    /**
     * 设置位置
     *
     * @return
     */
    protected int getGravity() {
        return Gravity.CENTER;
    }

    /**
     * 点击外面是否取消
     *
     * @return
     */
    protected boolean isClickOutSideCancel() {
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        //背景改透明
        // getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        //去除半透明阴影
        if (isTransparent()) {
            params.dimAmount = 0.0f;
        }
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        if (getAnimation() != -1) {
            params.windowAnimations = getAnimation();
        }
        params.gravity = getGravity();
        getDialog().setCancelable(isClickOutSideCancel());
        getDialog().setCanceledOnTouchOutside(isClickOutSideCancel());
        getDialog().getWindow().setAttributes(params);
    }
}
