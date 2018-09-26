package com.galvani.egon.connectionmonitor.Object;

/*
 * @author Egon Galvani
 * @description Class to storage connection and its ip location
 * */
public class LocatedConnection {
    private Connection connection;
    private Position position;

    public LocatedConnection(Connection connection, Position position) {
        this.connection = connection;
        this.position = position;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}
