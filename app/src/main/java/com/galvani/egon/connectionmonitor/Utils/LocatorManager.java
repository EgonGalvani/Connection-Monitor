package com.galvani.egon.connectionmonitor.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.galvani.egon.connectionmonitor.Object.Connection;
import com.galvani.egon.connectionmonitor.Object.LocatedConnection;
import com.galvani.egon.connectionmonitor.Object.Position;
import com.galvani.egon.connectionmonitor.Settings;
import com.google.gson.Gson;

import java.util.ArrayList;

/*
* @author Egon Galvani
 */
public class LocatorManager extends AsyncTask<Void, LocatedConnection, Void> {

    // instance of interface for events
    private LocatorEvents locatorEvents;

    // connections to parse
    private ArrayList<Connection> connections;

    // shared preference in which are stored the ip that has been already
    // searched in the database (for better performance)
    private SharedPreferences sharedPreferences;

    private DatabaseOpenHelper databaseOpenHelper;

    public LocatorManager(DatabaseOpenHelper databaseOpenHelper, Context context, LocatorEvents locatorEvents) {
        // set the shared pref
        sharedPreferences = context.getSharedPreferences(Settings.SHARED_PREF_IP_NAME, Context.MODE_PRIVATE);

        // set the interface
        this.locatorEvents = locatorEvents;

        this.databaseOpenHelper = databaseOpenHelper;
    }

    // set the connections to parse
    public void setConnections(ArrayList<Connection> connections) {
        this.connections = connections;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // locate every connection
        for (final Connection connection : connections) {
            // check if the connection has been already located
            Position position = findPosInPref(connection.getAddress());

            // if is already located take the position from the sharedPreference
            if (position != null) {
                publishProgress(new LocatedConnection(connection, position));
            } else {
                // get the ipNumber thanks to HexUtils
                long ipNumber = HexUtils.ipToIpNumber(connection.getAddress());

                // get the ip position from the database and save it in sharedPref if is different from null
                position = databaseOpenHelper.getIpPosition(String.valueOf(ipNumber));
                if (position != null) {
                    savePosInPref(connection.getAddress(), position);
                    publishProgress(new LocatedConnection(connection, position));
                }
            }
        }

        // location process finished
        return null;
    }

    @Override
    protected void onProgressUpdate(LocatedConnection... locatedConnection) {
        locatorEvents.onConnectionLocated(locatedConnection[0]);
    }

    // find the location of the ip in the shared preferences
    // return the position if found, otherwise return null
    private Position findPosInPref(String ip) {
        String posString = sharedPreferences.getString(ip, "");
        Position position = null;
        Gson gson = new Gson();

        if (!posString.isEmpty())
            position = gson.fromJson(posString, Position.class);

        return position;
    }

    // save the position relative to an ip in the shared preferences
    private void savePosInPref(String ip, Position position) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        editor.putString(ip, gson.toJson(position));
        editor.apply();
    }

    // interface for events
    public interface LocatorEvents {
        public void onConnectionLocated(LocatedConnection locatedConnection);
    }
}
