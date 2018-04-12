package com.sfh.lib.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.widget.Toast;

/**
 * 权限申请
 */

public class UtilPermission {
    public interface IPermission {
        /***
         * 权限申请成功
         * @param code
         */
        void onPermissionSuccess(int code);
        /***
         * 权限申请用户提示
         * @param code
         */
        void onPermissionRationale(int code);

        /***
         * 权限申请失败
         * @param code
         */
        void onPermissionFial(int code);
    }

    /**
     * 摄像头code
     */
    public static final int CODE_CAMERA = 0;
    /**
     * 联系人
     */
    public static final int CODE_CONTACTS = 1;
    /**
     * 日历数据
     */
    public static final int CODE_CALENDAR = 2;
    /**
     * 传感器
     */
    public static final int CODE_SENSORS = 3;
    /**
     * 麦克风
     */
    public static final int CODE_AUDIO = 4;
    /**
     * 短信
     */
    public static final int CODE_SMS = 5;
    /**
     * 存储
     */
    public static final int CODE_STORAGE = 6;
    /**
     * 位置
     */
    public static final int CODE_LOCATION = 7;

    /**
     * 电话
     */
    public static final int CODE_PHONE = 8;


    private static String[] PERMISSION_MULIT = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.SEND_SMS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CALL_PHONE
    };


    /***
     * 检查是否需要申请权限
     * @param activity
     * @param permissionCode
     * @param callback
     */
    public static void checkSelfPermission(@NonNull final Activity activity, @NonNull final int permissionCode, @NonNull IPermission callback) {


        if (permissionCode < 0 || permissionCode >= PERMISSION_MULIT.length) {
            return;
        }

        final String permission = PERMISSION_MULIT[permissionCode];

        // 手机android 系统版本小于 23 (android 6.0)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //一般android6以下会在安装时自动获取权限
            callback.onPermissionSuccess(permissionCode);
            return;
        }

        //检查应用是否拥有该权限
        boolean per = checkVersion(activity, permission);

        // 需要申请
        if (!per) {

            //为申请权限，系统会弹出对话框，询问用户是否给予应用授权该权限，用户可以选择允许或拒绝 (小米一样，永远只会 false)
            //华为部分权限的 ActivityCompat.shouldShowRequestPermissionRationale(Activity, String) 返回 false
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                callback.onPermissionRationale(permissionCode);
            }
            ActivityCompat.requestPermissions(activity, new String[]{permission}, permissionCode);
        } else {
            //有权限，不需要申请
            callback.onPermissionSuccess(permissionCode);
        }
    }

    private static boolean checkVersion(@NonNull final Activity activity, @NonNull String permission) {

        //targetSdkVersion<23时 即便运行在android6及以上设备 ContextCompat.checkSelfPermission中Context.checkSelfPermission失效
        //返回值始终为PERMISSION_GRANTED
        //此时必须使用PermissionChecker.checkSelfPermission

        // 应用程序targetSdkVersion目标版本
        int targetSdkVersion = activity.getApplication().getApplicationInfo().targetSdkVersion;
        //检查应用是否拥有该权限，被授权返回值为PERMISSION_GRANTED，否则返回PERMISSION_DENIED
        if (targetSdkVersion >= Build.VERSION_CODES.M) {

            return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;

        } else {
            return PermissionChecker.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
        }
    }

    /***
     *在Activity,Fragmnet回调 onRequestPermissionsResult
     * @param activity
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @param callback
     */
    public static void onRequestPermissionsResult(@NonNull final Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, @NonNull IPermission callback) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            callback.onPermissionSuccess(requestCode);
        } else {
            // 失败，打开应用设置权限【应用申请权限时，用户可能禁止或勾选了不在提示】
            openAppSetting(activity);
        }
    }

    private static void openAppSetting(@NonNull Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        if (activity.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
            try {
                activity.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(activity, "请打开应用管理,设置应用权限", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(activity, "请打开应用管理,设置应用权限", Toast.LENGTH_SHORT).show();
        }

    }
}
