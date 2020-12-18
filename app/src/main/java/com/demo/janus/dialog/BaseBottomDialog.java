package com.demo.janus.dialog;

import android.view.Gravity;

import com.demo.janus.R;

/**
 * @Author before
 * @Date 2020/6/11
 * @desc
 */
public abstract class BaseBottomDialog extends BaseDialog {

    @Override
    protected int getAnimation() {
        return R.style.common_BottomDialogAnimation;
    }

    @Override
    protected int getGravity() {
        return Gravity.BOTTOM;
    }

    @Override
    protected boolean isFullScreen() {
        return true;
    }
}
