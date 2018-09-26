package com.galvani.egon.connectionmonitor.Object;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

/*
 * @author Egon Galvani
 * @description This class store all connections with the same position,
 *      it is important to store also the marker because is the only way
 *      to manage the Marker Click event by the map
 * */
public class ConnectionMarker {
    private Marker marker;
    private Position position;
    private ArrayList<Connection> connections;

    public ConnectionMarker(Marker marker, Position position, ArrayList<Connection> connections) {
        this.marker = marker;
        this.position = position;
        this.connections = connections;
    }

    public Marker getMarker() {
        return marker;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public ArrayList<Connection> getConnections() {
        return connections;
    }

    public void addConnection(Connection connection) {
        connections.add(connection);
    }
}
