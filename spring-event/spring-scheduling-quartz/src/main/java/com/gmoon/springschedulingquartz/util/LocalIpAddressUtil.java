package com.gmoon.springschedulingquartz.util;

import com.gmoon.springschedulingquartz.exception.NotSupportedInitializerException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class LocalIpAddressUtil {
  private LocalIpAddressUtil() {
    throw new NotSupportedInitializerException();
  }

  public static List<String> getIpAddresses() {
    List<String> ipAddresses = new ArrayList<>();
    try {
      Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
      while (en.hasMoreElements()) {
        NetworkInterface network = en.nextElement();
        addIpAddress(ipAddresses, network);
      }
    } catch (SocketException e) {
      throw new RuntimeException("Couldn't get host name", e);
    }
    return ipAddresses;
  }

  private static void addIpAddress(List<String> ipAddresses, NetworkInterface network) {
    Enumeration<InetAddress> inetAddresses = network.getInetAddresses();
    while (inetAddresses.hasMoreElements()) {
      InetAddress inetAddress = inetAddresses.nextElement();

      boolean isLocalAddress = inetAddress.isSiteLocalAddress();
      boolean isNotLoopbackAddress = !inetAddress.isLoopbackAddress();
      boolean isNotLinkLocalAddress = !inetAddress.isLinkLocalAddress();
      if (isLocalAddress && isNotLoopbackAddress && isNotLinkLocalAddress) {
        String ip = inetAddress.getHostAddress();
        ipAddresses.add(ip);
      }
    }
  }
}
