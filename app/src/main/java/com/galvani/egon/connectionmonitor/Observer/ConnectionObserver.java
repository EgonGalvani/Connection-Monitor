package com.galvani.egon.connectionmonitor.Observer;

import android.os.AsyncTask;

import com.galvani.egon.connectionmonitor.Object.Connection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * @author Egon Galvani
 * @description Class to get the list of current connections
 * */

public class ConnectionObserver extends AsyncTask<Void, ConnectionObserver.ProgressObj, Void> {

    // regex for ipv4 and ipv6 connections (regex for file line)
    private static final String PATTERN_6 = "\\d+:\\s[0-9A-F]{32}:[0-9A-F]{4}\\s([0-9A-F]{32}):([0-9A-F]{4})\\s([0-9A-F]{2})\\s([0-9]{8}):([0-9]{8})\\s[0-9]{2}:[0-9]{8}\\s[0-9]{8}\\s+([0-9]+)";
    private static final String PATTERN_4 = "\\d+:\\s[0-9A-F]{8}:[0-9A-F]{4}\\s([0-9A-F]{8}):([0-9A-F]{4})\\s([0-9A-F]{2})\\s([0-9A-F]{8}):([0-9A-F]{8})\\s[0-9]{2}:[0-9]{8}\\s[0-9A-F]{8}\\s+([0-9]+)";
    // instance of interface
    private ObserverEvents observerEvents;

    // the interface is initialize
    public ConnectionObserver(ObserverEvents observerEvents) {
        this.observerEvents = observerEvents;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        for (Proto.PROTO CURRENT_PROTO : Proto.PROTO.values()) {
            ArrayList<Connection> connections = new ArrayList<>();
            String path = Proto.getPath(CURRENT_PROTO);
            boolean isIpv4 = Proto.isV4(CURRENT_PROTO);

            // read connection file
            try {
                File connFile = new File(path);
                BufferedReader reader = new BufferedReader(new FileReader(connFile));
                StringBuilder builder = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                String fileContent = builder.toString();

                // look for regex matches inside the file
                String regex = (isIpv4) ? PATTERN_4 : PATTERN_6;
                Matcher matcher = Pattern.compile(regex,
                        Pattern.CASE_INSENSITIVE | Pattern.UNIX_LINES | Pattern.DOTALL).matcher(fileContent);

                // parse every connection found
                while (matcher.find()) {
                    // fill the fields and create a connection object
                    String address = matcher.group(1);
                    String port = matcher.group(2);
                    String connectionState = matcher.group(3);
                    String uid = matcher.group(6);

                    Connection connection = new Connection(address, port, connectionState, uid, CURRENT_PROTO);

                    // add the parsed connection to the list of current file
                    connections.add(connection);
                }

                // for every file return the list of connections and protocol
                publishProgress(new ProgressObj(connections, CURRENT_PROTO));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    // trigger the OnConnectionParsed event
    @Override
    protected void onProgressUpdate(ProgressObj... progressObjs) {
        ProgressObj progressObj = progressObjs[0];
        observerEvents.onConnectionParsed(progressObj.getConnections());
    }

    // interface for events
    public interface ObserverEvents {
        // event called when every file has been parsed
        // params: the list of connections and the connection protocol of the file parsed
        public void onConnectionParsed(ArrayList<Connection> connections);
    }

    // Class container for list of connections and protocol
    public class ProgressObj {
        private ArrayList<Connection> connections;
        private Proto.PROTO proto;

        public ProgressObj(ArrayList<Connection> connections, Proto.PROTO proto) {
            this.connections = connections;
            this.proto = proto;
        }

        public Proto.PROTO getProto() {
            return proto;
        }

        public ArrayList<Connection> getConnections() {

            return connections;
        }
    }
}
