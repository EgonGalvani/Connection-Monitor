package com.galvani.egon.connectionmonitor.Utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.galvani.egon.connectionmonitor.R;

/*
 *    @author Egon Galvani
 *    @description  Class to storage and obtain more details about one app
 */

public class AppInfo {
    private int uid;
    private String appName;
    private String appPackage;
    private Drawable appIcon;

    public AppInfo(int uid, Context context) {
        this.uid = uid;

        // default values
        appPackage = "Package Not Found";
        appName = "App Name Not Found";
        appIcon = ContextCompat.getDrawable(context, R.drawable.ic_android_black_24dp);

        // get the package of every application from the connection uid
        String[] packages = context.getPackageManager().getPackagesForUid(uid);
        if (packages != null) {
            appPackage = packages[0];

            // thanks to the package (if is different from null) obtain other app info
            try {
                ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(appPackage, 0);

                appName = String.valueOf(context.getPackageManager().getApplicationLabel(applicationInfo));
                appIcon = context.getPackageManager().getApplicationIcon(applicationInfo);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // getter methods
    public String getAppName() {
        return appName;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }
}
