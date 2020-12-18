package com.demo.janus.permission;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Author before
 * @Date 2020/6/10
 * @desc
 */
public class PermissionFragment extends Fragment {
    private SparseArray<IPermissionListener> mCallbacks = new SparseArray<>();
    private Random mCodeGenerator = new Random();
    private FragmentActivity mActivity;

    public PermissionFragment() {
    }

    public static PermissionFragment getInstance() {
        return new PermissionFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置为 true，表示 configuration change 的时候，fragment 实例不会背重新创建
        setRetainInstance(true);
        mActivity = getActivity();
    }

    public void requestPermissions(String[] permissions, IPermissionListener callback) {
        // 兼容6.0以下版本
        if (checkVersion(permissions, callback)) return;

        int requestCode = makeRequestCode();
        mCallbacks.put(requestCode, callback);
        requestPermissions(permissions, requestCode);
    }

    /**
     * 如果小于等于android 6.0，则不需要申请
     *
     * @param permissions
     * @param callback
     * @return
     */
    private boolean checkVersion(String[] permissions, IPermissionListener callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (callback instanceof ISimplePermissionListener) {
                ((ISimplePermissionListener) callback).onAccepted(true);
            } else if (callback instanceof IEachPermissionListener) {
                Permission per;
                for (String permission : permissions) {
                    per = new Permission();
                    per.name = permission;
                    per.granted = true;
                    ((IEachPermissionListener) callback).onAccepted(per);
                }
            } else if (callback instanceof IListPermissionListener) {
                ((IListPermissionListener) callback).onGranted();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        IPermissionListener callback = mCallbacks.get(requestCode);
        mCallbacks.remove(requestCode);

        if (callback == null) return;

        List<Permission> refusedPermissions = new ArrayList<>(grantResults.length);
        List<Permission> grantedPermissions = new ArrayList<>(grantResults.length);
        Permission permission;
        for (int i = 0; i < grantResults.length; i++) {
            permission = new Permission();
            permission.name = permissions[i];
            int grantResult = grantResults[i];
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                permission.granted = true;
                grantedPermissions.add(permission);
            } else {
                permission.granted = false;
                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permissions[i])) {
                    permission.shouldShowRequestPermissionRationale = true;
                } else {
                    permission.shouldShowRequestPermissionRationale = false;
                }
                refusedPermissions.add(permission);
            }
        }

        if (callback instanceof ISimplePermissionListener) {
            ((ISimplePermissionListener) callback).onAccepted(refusedPermissions.size() == 0);
        } else if (callback instanceof IEachPermissionListener) {
            for (Permission grantedPermission : grantedPermissions) {
                ((IEachPermissionListener) callback).onAccepted(grantedPermission);
            }
            for (Permission refusedPermission : refusedPermissions) {
                ((IEachPermissionListener) callback).onAccepted(refusedPermission);
            }
        } else if (callback instanceof IListPermissionListener) {
            if (refusedPermissions.size() == 0) {
                ((IListPermissionListener) callback).onGranted();
            } else {
                ((IListPermissionListener) callback).onRefused(refusedPermissions);
            }
        }
    }

    /**
     * 随机生成唯一的requestCode，最多尝试10次
     *
     * @return
     */
    private int makeRequestCode() {
        int requestCode = 0;
        int tryCount = 0;
        do {
            requestCode = mCodeGenerator.nextInt(0x0000FFFF);
            tryCount++;
        } while (mCallbacks.indexOfKey(requestCode) >= 0 && tryCount < 10);
        requestCode = mCodeGenerator.nextInt(0x0000FFFF);
        return requestCode;
    }

}
