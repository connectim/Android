package com.wallet.utils;

import java.security.SecureRandom;

public class WalletUtil {

    /**
     * 获取随机数种子
     */
    public static String getRandomSeed(){
        String random = bytesToHexString(SecureRandom.getSeed(64));
        return random;
    }

    /**
     * byte转Hex
     */
    public synchronized static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * Hex转byte
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) ( (byte) "0123456789ABCDEF".indexOf(hexChars[pos]) <<
                    4 | (byte) "0123456789ABCDEF".indexOf(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * 两个字符串做异或
     */
    public static String xor(String strHex1,String strHex2){
        if(strHex1.length() != strHex2.length()){
            return "";
        }
        byte[] byte1 = hexStringToBytes(strHex1);
        byte[] byte2 = hexStringToBytes(strHex2);
        byte[] valueByte = xor(byte1, byte2);
        return bytesToHexString(valueByte);
    }

    /**
     * 两个byte[]做异或
     */
    public static byte[] xor(byte[] byte1, byte[] byte2) {
        if(byte1.length != byte2.length){
            return null;
        }
        byte[] index = new byte[byte1.length];
        for (int i = 0; i < byte1.length; i++) {
            index[i] = (byte) (byte1[i] ^ byte2[i]);
        }
        return index;
    }

}
