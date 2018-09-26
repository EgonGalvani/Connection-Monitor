package com.galvani.egon.connectionmonitor.Object;

import com.galvani.egon.connectionmonitor.Observer.Proto;

/*
 * @author Egon Galvani
 * @description Class used to store data retrieved by files in 'proc/net'
 **/

public class Connection {
    private String address;
    private String port;
    private String connectionState;
    private String uid;
    private Proto.PROTO proto;

    public Connection(String address, String port, String connectionState, String uid, Proto.PROTO proto) {
        this.address = address;
        this.port = port;
        this.connectionState = connectionState;
        this.uid = uid;
        this.proto = proto;
    }

    public String getUid() {
        return uid;
    }

    public String getAddress() {
        return address;
    }

    public String getPort() {
        return port;
    }

    public String getConnectionState() {
        return connectionState;
    }

    public Proto.PROTO getProto() {
        return proto;
    }

    public void setProto(Proto.PROTO proto) {
        this.proto = proto;
    }
}
