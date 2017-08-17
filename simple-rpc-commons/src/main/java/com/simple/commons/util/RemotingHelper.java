package com.simple.commons.util;

import java.net.InetAddress;

public class RemotingHelper {


    public static String getHostAddr() {
        try {

            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
