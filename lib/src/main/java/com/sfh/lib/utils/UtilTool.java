package com.sfh.lib.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * 功能描述:基本工具
 *
 * @author SunFeihu 孙飞虎
 * @date 2017/7/20
 */
public final class UtilTool {

    private UtilTool() {

        throw new IllegalStateException ("you can't instantiate me!");
    }

    /**
     * 验证手机格式
     */
    public static boolean isMobile(String mobiles) {

        if (TextUtils.isEmpty (mobiles)) {
            return false;
        } else {
            String telRegex = "[1]\\d{10}";
            return mobiles.matches (telRegex);
        }

    }

    /**
     * 方法描述:网络连接 boolean
     *
     * @param context
     * @return
     */
    public static boolean isConnect(Context context) {

        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService (Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                // 获取网络连接管理的对象
                NetworkInfo info = connectivity.getActiveNetworkInfo ();
                if (info != null && info.isConnected ()) {
                    // 判断当前网络是否已经连接
                    if (info.getState () == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 获取当前手机电话号码
     */
    public static String getPhoneNumber(Context context) {

        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService (Context.TELEPHONY_SERVICE);
        @SuppressLint("HardwareIds")
        String number = telephonyManager.getLine1Number ();
        return TextUtils.isEmpty (number) ? "" : number;
    }

    /**
     * 获取硬件设备信息组合唯一号码
     * <p>
     * 需要
     * android.permission.ACCESS_WIFI_STATE
     * android.Manifest.permission.READ_PHONE_STATE，
     * android.Manifest.permission.READ_SMS,
     * android.Manifest.permission.READ_PHONE_NUMBERS 权限
     *
     * @return wifi mac地址 + android设备唯一ID + android设备唯一标识
     */
    public static String getMacDeviceId(Context context) {

        String mac = "";
        String androidId = "";
        String deviceId = "";
        String imei = "";
        String meid = "";
        final Context temp = context.getApplicationContext ();
        try {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                // Android  6.0 之前（不包括6.0）
                WifiManager wifiMng = (WifiManager) temp.getSystemService (Context.WIFI_SERVICE);
                WifiInfo wifiInfor = wifiMng.getConnectionInfo ();
                mac = wifiInfor.getMacAddress ();
            } else if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                // Android 7.0之前（不包括）
                mac = new BufferedReader (new FileReader (new File ("/sys/class/net/wlan0/address"))).readLine ();
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

                //Android  8.0 之前（不包括8.0） 序列号
                Class c = Class.forName("android.os.SystemProperties");
                    Method get = c.getMethod("get", String.class);
                mac = (String) get.invoke(c, "ro.serialno");

            }else{
                //Android  8.0 以上（包括8.0） 序列号
                mac = Build.getSerial ();
            }

        } catch (Exception e) {
            e.printStackTrace ();
        }

        try {
            androidId = Settings.Secure.getString (temp.getContentResolver (), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace ();
        }
        try {
            TelephonyManager telephonyManager = (TelephonyManager) temp.getSystemService (Context.TELEPHONY_SERVICE);

            deviceId = telephonyManager.getDeviceId ();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imei = telephonyManager.getImei ();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                meid = telephonyManager.getMeid ();
            }
        } catch (Exception e) {
        }

        return mac + androidId + deviceId + imei + meid;
    }


    /***
     * 设置输入框，在输入参数数据时，保存几位小数
     * @param et 输入控件
     * @param size 小数点
     */
    public static void setEditTextInputSize(EditText et, final int size) {

        if (et == null) {
            return;
        }
        et.setInputType (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        et.setFilters (new InputFilter[]{
                new InputFilter () {

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

//                        CharSequence source,  //输入的文字
//                        int start,  //开始位置
//                        int end,  //结束位置
//                        Spanned dest, //当前显示的内容
//                        int dstart,  //当前开始位置
//                        int dend //当前结束位置

                        // 内容长度
                        int cl = dest.length ();

                        // 输入的文字是.
                        if (TextUtils.equals (".", source)) {

                            //输入位置在第1位置
                            if (dstart == 0) {

                                // 当前输入框是否包含.
                                if (TextUtils.indexOf (dest, '.') > 0) {
                                    return "";
                                }
                                // 内容长度不能大于限制长度
                                if (cl > size) {
                                    return "";
                                }
                                return "0.";

                            } else {
                                // 已输入中间输入.
                                if (TextUtils.indexOf (dest, '.') > 0) {
                                    return "";
                                }
                                // 点的位置，后面有几位
                                if (cl - dstart > size) {
                                    return "";
                                }
                            }
                            return null;
                        }

                        // 输入其他内容
                        if (isEmpty (dest)) {
                            return null;
                        }

                        // 是否包含.
                        int index = TextUtils.indexOf (dest, '.');

                        if (index > 0) {
                            //点后面长度
                            int length = TextUtils.substring (dest, index + 1, cl).length ();
                            if (length == size && dend > index) {
                                return "";
                            }
                        }

                        return null;
                    }
                }
        });
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号名称 versionName
     */
    public static String getVersion(Context context) {

        try {
            PackageManager manager = context.getPackageManager ();
            PackageInfo info = manager.getPackageInfo (context.getPackageName (), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace ();
            return "";
        }
    }

    /**
     * 获取版本号code
     *
     * @return 当前应用的版本号 versionCode
     */
    public static String getVersionCode(Context context) {

        try {
            PackageManager manager = context.getPackageManager ();
            PackageInfo info = manager.getPackageInfo (context.getPackageName (), 0);
            String version = String.valueOf (info.versionCode);
            return version;
        } catch (Exception e) {
            e.printStackTrace ();
            return "";
        }
    }

    /***
     * 获取当前进程名
     * @param cxt
     * @param pid
     * @return
     */
    public static String getProcessName(Context cxt, int pid) {

        ActivityManager am = (ActivityManager) cxt.getSystemService (Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses ();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }


    /***
     * 设置复制内容到复制系统[配合getCopyText一起使用]
     * @param label 描述
     * @param content 复制内容
     * @return
     */
    public static boolean setCopyText(Context context, String label, String content) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService (Context.CLIPBOARD_SERVICE);
            if (clipboardManager == null) {
                return false;
            }
            clipboardManager.setPrimaryClip (ClipData.newPlainText (label, content));
            return true;
        } else {
            android.text.ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService (Context.CLIPBOARD_SERVICE);
            if (clipboardManager == null) {
                return false;
            }
            clipboardManager.setText (content);
            return true;
        }
    }

    /***
     * 获取复制内容
     * @return
     */
    public static CharSequence getCopyText(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService (Context.CLIPBOARD_SERVICE);
            if (clipboardManager == null || !clipboardManager.hasPrimaryClip ()) {
                return "";
            }

            // 检查剪贴板是否有内容
            ClipData clipData = clipboardManager.getPrimaryClip ();
            if (clipData == null && clipData.getItemCount () <= 0) {
                return "";
            }

            ClipData.Item item =  clipData.getItemAt (0);
            if (item == null){
                return "";
            }
            return item.getText ();

        } else {
            android.text.ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService (Context.CLIPBOARD_SERVICE);
            if (clipboardManager == null || !clipboardManager.hasText ()) {
                return "";
            }
            return clipboardManager.getText ();
        }
    }
}
