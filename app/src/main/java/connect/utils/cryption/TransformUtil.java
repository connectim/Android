package connect.utils.cryption;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 常用的数值变换工具类
 * Created by Administrator on 2017/10/24.
 */
public class TransformUtil {

    public static String SHA_256 = "SHA-256";
    public static String MD5 = "MD5";

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
