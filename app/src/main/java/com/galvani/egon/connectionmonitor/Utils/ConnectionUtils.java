package com.galvani.egon.connectionmonitor.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/*
 *   @author Egon Galvani
 */

public class ConnectionUtils {

    private Context context;

    public ConnectionUtils(Context context) {
        this.context = context;
    }

    // return true if network is available, otherwise false
    public boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return false;
    }
}
