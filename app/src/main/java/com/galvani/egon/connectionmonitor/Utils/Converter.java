package com.galvani.egon.connectionmonitor.Utils;

import com.galvani.egon.connectionmonitor.Object.Connection;
import com.galvani.egon.connectionmonitor.Observer.Proto;

/*
 * @author Egon Galvani
 * @description Class used to convert data from 'proc/net' files format to standard format
 */

public class Converter {

    // convert the connection data from hex to dot notation for ip
    // and from hex to decimal for the port, reciveQueue and trasmitQueue
    public static Connection convert(Connection connection) {
        String ip = (Proto.isV4(connection.getProto()))
                ? HexUtils.hexToIpv4(connection.getAddress()) : HexUtils.hexToIpv6(connection.getAddress());
        ip = HexUtils.dotSwapIp(ip);

        String port = String.valueOf(HexUtils.hexToInt(connection.getPort()));

        return new Connection(ip, port, connection.getConnectionState(), connection.getUid(), connection.getProto());
    }
}
