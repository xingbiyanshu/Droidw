package com.sissi.droidw.settings;

import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;

/**
 * Created by Sissi on 2018/5/17.
 *
 * 检查各设置项的状态
 */

public class Checker {

    /** 应用通知权限是否已开启 
     * */
    public static boolean isAppNotificationEnabled(Context context){
        return NotificationManagerCompat.from(context).areNotificationsEnabled(); // 注意: API level 19以下始终返回true
    }

    /** 应用自启动(开机后自启动,被杀后自启动)权限是否已开启 
     * */
    public static boolean isAppAutoLaunchEnabled(Context context){
        return false;
    }
}
