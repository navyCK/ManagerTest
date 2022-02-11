package com.herblinker.android.libraries.base.device;

import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;

import com.herblinker.libraries.base.data.DataTransfer;

import java.nio.ByteOrder;

public class NetworkHelper {
    public static String getIp(WifiInfo wifiInfo){
        return intIptoStringIp(wifiInfo.getIpAddress());
    }
    public static String getIp(DhcpInfo dhcpInfo){
        return intIptoStringIp(dhcpInfo.gateway);
    }
    private static String intIptoStringIp(int ip){
        if(ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN))
            ip=Integer.reverseBytes(ip);
        byte[] ipByteArray = DataTransfer.integerToBytes(ip);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ipByteArray[0]&0x000000FF);
        stringBuilder.append('.');
        stringBuilder.append(ipByteArray[1]&0x000000FF);
        stringBuilder.append('.');
        stringBuilder.append(ipByteArray[2]&0x000000FF);
        stringBuilder.append('.');
        stringBuilder.append(ipByteArray[3]&0x000000FF);
        return stringBuilder.toString();
    }
    public static boolean isSameName(String a, String b){
        if(a.startsWith("\""))
            if(a.endsWith("\""))
                a=a.substring(1, a.length()-1);
        if(b.startsWith("\""))
            if(b.endsWith("\""))
                b=b.substring(1, b.length()-1);
        return a.equals(b);
    }
}
