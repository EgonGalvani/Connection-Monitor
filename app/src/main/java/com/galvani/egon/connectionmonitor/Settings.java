package com.galvani.egon.connectionmonitor;

public class Settings {

    // connection files path
    public static final String PATH = "/proc/net/";

    // database name and version
    public static final String DB_NAME = "Ip2LocationDB.sqlite";
    public static final int DB_VERSION = 3;

    // default refresh time
    public static final int DEFAULT_REFRESH_DELAY = 15;

    // sharedPreferences name
    public static final String SHARED_PREF_TIMER_NAME = "delay_timer";
    public static final String SHARED_PREF_IP_NAME = "common_ip_location";
}
