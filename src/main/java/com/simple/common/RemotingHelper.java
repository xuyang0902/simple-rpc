package com.simple.common;

import io.netty.channel.Channel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class RemotingHelper {

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static String exceptionSimpleDesc(final Throwable e) {
        StringBuffer sb = new StringBuffer();
        if (e != null) {
            sb.append(e.toString());

            StackTraceElement[] stackTrace = e.getStackTrace();
            if (stackTrace != null && stackTrace.length > 0) {
                StackTraceElement elment = stackTrace[0];
                sb.append(", ");
                sb.append(elment.toString());
            }
        }

        return sb.toString();
    }

    public static SocketAddress string2SocketAddress(final String addr) {
        String[] s = addr.split(":");
        return new InetSocketAddress(s[0], Integer.parseInt(s[1]));
    }

    public static String parseChannelRemoteAddr(final Channel channel) {
        if (null == channel) {
            return "";
        }
        SocketAddress remote = channel.remoteAddress();
        final String addr = remote != null ? remote.toString() : "";

        if (addr.length() > 0) {
            int index = addr.lastIndexOf("/");
            if (index >= 0) {
                return addr.substring(index + 1);
            }

            return addr;
        }

        return "";
    }

    public static String parseChannelRemoteName(final Channel channel) {
        if (null == channel) {
            return "";
        }
        final InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();
        if (remote != null) {
            return remote.getAddress().getHostName();
        }
        return "";
    }

    public static String parseSocketAddressAddr(SocketAddress socketAddress) {
        if (socketAddress != null) {
            final String addr = socketAddress.toString();

            if (addr.length() > 0) {
                return addr.substring(1);
            }
        }
        return "";
    }

    public static String parseSocketAddressName(SocketAddress socketAddress) {

        final InetSocketAddress addrs = (InetSocketAddress) socketAddress;
        if (addrs != null) {
            return addrs.getAddress().getHostName();
        }
        return "";
    }


    public static String getHostAddr() {
        try {

            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
