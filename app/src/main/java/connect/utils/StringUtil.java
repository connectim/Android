package connect.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * String tool
 */
public class StringUtil {

    public static String SHA_256 = "SHA-256";
    public static String MD5 = "MD5";

    /** message header ext */
    public static final byte[] MSG_HEADER_EXI = new byte[]{(byte) 0xc0, (byte) 0x2E, (byte) 0xC7};

    /**
     * Remove non digital
     * @param string
     * @return
     */
    public static String filterNumber(String string) {
        return string.replaceAll("[^\\d]*", "");
    }

    /**
     * list to String
     * @param list
     * @return
     */
    public static String listToString(List<Integer> list){
        String value = "";
        for(Integer integer : list){
            value = value + integer;
        }
        return value;
    }

    public static String cdHash256(String value){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(value.getBytes());
            String hash = StringUtil.bytesToHexString(md.digest());
            return hash;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * byte to String
     * @param src
     * @return
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
     * String to byte
     * @param hexString
     * @return
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
     * encrypt Base64
     * @param value
     * @return
     */
    public static String encodeBase64(String value){
        String strBase64 = null;
        try {
            strBase64 = Base64.encodeToString(value.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return strBase64;
    }

    /**
     * Compare version size
     * @param versionServer
     * @param versionLocal
     * @return if version1 > version2, return 1, if equal, return 0, else return -1
     */
    public static int VersionComparison(String versionServer, String versionLocal) {
        String version1 = versionServer;
        String version2 = versionLocal;
        if (version1 == null || version1.length() == 0 || version2 == null || version2.length() == 0)
            throw new IllegalArgumentException("Invalid parameter!");

        int index1 = 0;
        int index2 = 0;
        while (index1 < version1.length() && index2 < version2.length()) {
            int[] number1 = getValue(version1, index1);
            int[] number2 = getValue(version2, index2);

            if (number1[0] < number2[0]) {
                return -1;
            } else if (number1[0] > number2[0]) {
                return 1;
            } else {
                index1 = number1[1] + 1;
                index2 = number2[1] + 1;
            }
        }
        if (index1 -1 == version1.length() && index2 - 1 == version2.length())
            return 0;
        if (index1 - 1 < version1.length())
            return 1;
        else
            return -1;
    }

    public static int[] getValue(String version, int index) {
        int[] value_index = new int[2];
        StringBuilder sb = new StringBuilder();
        while (index < version.length() && version.charAt(index) != '.') {
            sb.append(version.charAt(index));
            index++;
        }
        value_index[0] = Integer.parseInt(sb.toString());
        value_index[1] = index;

        return value_index;
    }

    public static byte[] digest(String encName, byte[] strSrc) {
        byte[] strDes = null;
        try {
            if (encName == null || encName.equals("")) {
                throw new RuntimeException("encName is Null");
            }
            MessageDigest md = MessageDigest.getInstance(encName);
            md.update(strSrc);
            strDes = md.digest();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return strDes;
    }
}
