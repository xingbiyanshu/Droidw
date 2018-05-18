package com.sissi.droidw.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

/**
 * Created by Sissi on 2018/5/17.
 *
 * 引导用户进入设置页面
 *
 */

public class Guider {

    public static Intent guide2AppNotification(Context context/*, String channel*/){
        String packageName = context.getPackageName();
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            if (channel != null) {
//                intent.setAction(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
//                intent.putExtra(Settings.EXTRA_CHANNEL_ID, channel);
//            } else {
//                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
//            }
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName);
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra("app_package", packageName);
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
        }else {
//            intent.setAction(Settings.ACTION_APPLICATION_SETTINGS);
//            intent.setAction(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
//            intent.setAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
//            intent.setAction(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + packageName));
        }

        return intent;
    }

    public static Intent guide2AppDetails(Context context){
        String packageName = context.getPackageName();
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + packageName));
        return intent;
    }

    public static Intent guide2AppAutoLaunch(Context context){
        return guide2AppDetails(context);
    }

}
