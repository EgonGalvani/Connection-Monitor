package com.galvani.egon.connectionmonitor.Utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import com.galvani.egon.connectionmonitor.Object.Position;
import com.galvani.egon.connectionmonitor.Settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/*
 * @author Egon Galvani
 * @description Helper to open/query the database
 */

public class DatabaseOpenHelper extends SQLiteOpenHelper implements Serializable {

    // to use the database is necessary copy it from the assets folder to the phone memory
    // this is the path of the database copied
    private String DBPath;
    // context
    private Context context;
    // instance of the db events interface
    private DBEvent dbEvent;

    // constructor
    public DatabaseOpenHelper(DBEvent dbEvent, Context context, String filePath) {
        super(context, Settings.DB_NAME, null, Settings.DB_VERSION);
        this.dbEvent = dbEvent;
        this.context = context;

        // the path of the database copy is created
        DBPath = String.valueOf(filePath + "/" + Settings.DB_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    // if the database is already copied from the assets trigger the interface event
    // otherwise create a DBCopyTask (an AsyncTask) to copy the database
    public void prepareDB() throws IOException {
        if (!checkDataBase()) {
            DBCopyTask dbCopyTask = new DBCopyTask(dbEvent);
            dbCopyTask.execute();
        } else dbEvent.onDbHelperReady();
    }

    // query the database to retrieve the data for the ip that has been passed
    // return null if the ip has not been found, otherwise a Position object with ip position details
    public Position getIpPosition(String ip) {
        SQLiteDatabase ip2LocDB = SQLiteDatabase.openDatabase(DBPath, null, SQLiteDatabase.OPEN_READONLY);
        String query = "select country_name, region_name, city_name, latitude, longitude" +
                " From Ip2LocationTable Where " + ip + " Between ip_from AND ip_to Limit 0, 1";
        Cursor cursor = ip2LocDB.rawQuery(query, null);

        if (cursor.moveToNext()) {
            Position position = new Position(cursor.getString(0), cursor.getString(1),
                    cursor.getString(2), cursor.getFloat(3), cursor.getFloat(4));
            ip2LocDB.close();
            cursor.close();
            return position;
        } else {
            return null;
        }
    }

    // check if the database has been already copied
    private boolean checkDataBase() {
        boolean checkDB = false;
        try {
            File dbFile = new File(DBPath);
            checkDB = dbFile.exists();
        } catch (SQLiteException ignored) {
        }
        return checkDB;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    // interface for DB events
    public interface DBEvent {
        public void onDbHelperReady();
    }

    // The DBCopyTask takes care of copying the database from the assets to the path created
    // in the constructor. For a better performance this is done in an AsyncTask
    // Once the process is finished the dbEvents interface is triggered
    private class DBCopyTask extends AsyncTask<Void, Void, Void> {
        private DBEvent dbEvent;

        public DBCopyTask(DBEvent dbEvent) {
            this.dbEvent = dbEvent;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                OutputStream myOutput = new FileOutputStream(DBPath);
                InputStream myInput = context.getAssets().open(Settings.DB_NAME);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }
                myOutput.flush();
                myOutput.close();
                myInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dbEvent.onDbHelperReady();
        }
    }
}
