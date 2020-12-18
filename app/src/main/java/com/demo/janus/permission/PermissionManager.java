package com.demo.janus.permission;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

/**
 * @Author before
 * @Date 2020/6/10
 * @desc
 */
public class PermissionManager {
    private static final String TAG_PERMISSION = "tag_permission";
    private static PermissionManager instance;

    private PermissionManager() {
    }

    public static PermissionManager getInstance() {
        if (instance == null) {
            instance = new PermissionManager();
        }
        return instance;
    }

    public void requestPermissions(FragmentActivity activity, String[] permissions, IPermissionListener callback) {
        getPermissionFragment(activity).requestPermissions(permissions, callback);
    }

    private PermissionFragment getPermissionFragment(FragmentActivity activity) {
        PermissionFragment fragment = (PermissionFragment) activity.getSupportFragmentManager().findFragmentByTag(TAG_PERMISSION);
        if (fragment == null) {
            fragment = PermissionFragment.getInstance();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .add(fragment, TAG_PERMISSION)
                    .commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return fragment;
    }

    /**
     * 用户勾选不再显示并点击拒绝，弹出打开设置页面申请权限，也可以自定义实现
     *
     * @param context Context
     * @param title   弹窗标题
     * @param message 申请权限解释说明
     * @param confirm 确认按钮的文字，默认OK
     * @param cancel  取消按钮呢的文字，默认不显示取消按钮
     */

    public void requestDialogAgain(final Activity context, @NonNull String title, @NonNull String message, String confirm, String cancel) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle(title).setMessage(message);
            builder.setPositiveButton(confirm == null ? "OK" : confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startSettingActivity(context);
                    dialog.dismiss();
                }
            });
            if (null != cancel) {
                builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
            builder.setCancelable(false);
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开设置页面打开权限
     *
     * @param context
     */
    public void startSettingActivity(@NonNull Activity context) {

        try {
            Intent intent =
                    new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" +
                            context.getPackageName()));
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            context.startActivityForResult(intent, 10); //这里的requestCode和onActivityResult中requestCode要一致
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
