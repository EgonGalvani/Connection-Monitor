package com.galvani.egon.connectionmonitor;

import android.app.Application;

import com.galvani.egon.connectionmonitor.Utils.DatabaseOpenHelper;

public class App extends Application {

    // the DatabaseOpenHelper is defined as a global variable for better performance
    public DatabaseOpenHelper databaseOpenHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        databaseOpenHelper = null;
    }
}
