package com.bupt.indooranalysis.Util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * 检查权限的工具类
 * <p/>
 * Created by wangchenlong on 16/1/26.
 */
public class PermissionsChecker {
    private final Context mContext;

    public static final String[] PERMISSIONS = new String[]{
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.READ_PHONE_STATE",
            "android.permission.READ_CONTACTS",
            "android.permission.GET_ACCOUNTS"
    };

    public PermissionsChecker(Context context) {
        mContext = context.getApplicationContext();
    }

    // 判断权限集合
    public boolean lacksPermissions(String... permissions) {
        for (String permission : permissions) {
            if (lacksPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否缺少权限
    private boolean lacksPermission(String permission) {
        PackageManager pm = mContext.getPackageManager();
        boolean mPermission = (PackageManager.PERMISSION_DENIED ==
                pm.checkPermission(permission, "com.bupt.indooranalysis"));
        return mPermission;
    }
}
