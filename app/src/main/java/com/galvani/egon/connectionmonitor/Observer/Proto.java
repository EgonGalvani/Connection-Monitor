package com.galvani.egon.connectionmonitor.Observer;

import com.galvani.egon.connectionmonitor.Settings;

/*
 * @author Egon Galvani
 * @description Class to manage protocols (utils and connection state)
 */
public class Proto {

    // get connection file for each protocol
    public static String getPath(PROTO proto) {
        String fileName = (proto == PROTO.TCP) ? "tcp"
                : (proto == PROTO.UDP) ? "udp"
                : (proto == PROTO.TCP6) ? "tcp6" : "udp6";
        return Settings.PATH + fileName;
    }

    // return true if the protocol is for ipv4 otherwise return false
    public static boolean isV4(PROTO proto) {
        return (proto == PROTO.TCP) || (proto == PROTO.UDP);
    }

    public static String getConnectionStateString(int code) {
        return (CONNECTION_STATE.values()[code - 1]).toString();
    }

    // list of protocol
    public enum PROTO {
        TCP, UDP, TCP6, UDP6
    }

    enum CONNECTION_STATE {
        TCP_ESTABLISHED,
        TCP_SYN_SENT,
        TCP_SYN_RECV,
        TCP_FIN_WAIT1,
        TCP_FIN_WAIT2,
        TCP_TIME_WAIT,
        TCP_CLOSE,
        TCP_CLOSE_WAIT,
        TCP_LAST_ACK,
        TCP_LISTEN,
        TCP_CLOSING,
        TCP_MAX_STATES
    }
}
