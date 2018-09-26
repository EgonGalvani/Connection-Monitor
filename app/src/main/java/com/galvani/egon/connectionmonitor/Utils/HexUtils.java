package com.galvani.egon.connectionmonitor.Utils;

/*
* @author Egon Galvani
* @description Class with utils methods for hex strings
 */
public class HexUtils {

    // reverse ip (from a.b.c.d -> d.c.b.a)
    public static String dotSwapIp(String ip) {
        String[] parts = ip.split("\\.");
        StringBuilder builder = new StringBuilder();

        for (int i = parts.length - 1; i >= 0; i--) {
            builder.append(parts[i]);
            if (i != 0)
                builder.append(".");
        }

        return builder.toString();
    }

    // hex string to ipv4 string
    public static String hexToIpv4(String hex) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < hex.length(); i = i + 2) {
            builder.append(Integer.valueOf(hex.substring(i, i + 2), 16));

            if (i != hex.length() - 2)
                builder.append(".");
        }

        return builder.toString();
    }

    // hex to ipv6 (ipv6 for the moment are treated like ipv4 converted in ipv6)
    public static String hexToIpv6(String hex) {
        return hexToIpv4(hex.substring(hex.length() - 8, hex.length()));
    }

    // hex to decimal
    public static int hexToInt(String hex) {
        return Integer.parseInt(hex, 16);
    }

    // ip to ipNumber ( link: https://www.ip2location.com/docs/db5-ip-country-region-city-latitude-longitude-specification.pdf)
    public static long ipToIpNumber(String ipAddress) {
        String[] ipAddressInArray = ipAddress.split("\\.");

        long result = 0;
        for (int i = 0; i < ipAddressInArray.length; i++) {
            int power = 3 - i;
            int ip = Integer.parseInt(ipAddressInArray[i]);
            result += ip * Math.pow(256, power);

        }

        return result;
    }
}
