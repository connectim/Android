package instant.utils;

/**
 * Canonical matching tool
 * Created by Administrator on 2016/8/26.
 */
public class RegularUtil {

    /** Telephone number rule */
    public static final String PHONE_NUMBER = "\\d{6,20}";

    /** Login password rules */
    public static final String LOGIN_PASSWORD = "[a-zA-Z0-9]{6,20}";

    /** All digital rule */
    public static final String ALL_NUMBER = "[0-9]*";

    /** Verification code rule */
    public static final String VERIFICATION_CODE = "[0-9]{4}";

    /** Network picture */
    public static final String VERIFICATION_HTTP = "^http.*";

    /** Input amount */
    public static final String VERIFICATION_AMOUT = "^[0-9]+(\\.?[0-9]+)$";

    /**  http/https address */
    public static final String VERIFICATION_URL_HEADER = "^(http|https)://.*";
    /** website address */
    public static final String VERIFYCATION_WEBURL = "(?:(?:(?:[a-z]+:)?//))?(?:localhost|(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])(?:\\.(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])){3}|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#][^\\s\"]*)?";
    /** expression */
    public static final String VERIFYCATION_EMJ = "\\[[^\\[]+\\]";
    /** transfer / lucky packet / gather */
    public static final String OUTER_BITWEBSITE = "(http|https)://(cd.snowball.io:5502|transfer.connect.im|luckypacket.connect.im)/share/v\\d/(transfer|packet|pay){1}\\?{1}.*";
    /** outer transfer */
    public static final String OUTER_BITWEBSITE_TRANSFER = "(http|https)://(cd.snowball.io:5502|transfer.connect.im)/share/v\\d/transfer\\?{1}.*";
    /** outer lucky packet */
    public static final String OUTER_BITWEBSITE_PACKET = "(http|https)://(cd.snowball.io:5502|luckypacket.connect.im)/share/v\\d/packet\\?{1}.*";
    /** gather */
    public static final String OUTER_BITWEBSITE_PAY = "(http|https)://(cd.snowball.io:5502|transfer.connect.im)/share/v\\d/pay\\?{1}.*";
    /** group avatar */
    public static final String GROUP_AVATAR = "%1$s/avatar/v1/group/%2$s.jpg";
    /** password */
    public static final String PASSWORD = "^(?!^(\\d+|[a-zA-Z]+|[`~!@#\\$%\\^&*\\(\\)\\-_=\\+\\\\\\|\\[\\]\\{\\}:;\\\"\\',.<>\\/\\?]+)$)^[\\w`~!@#\\$%\\^&*\\(\\)\\-_=\\+\\\\\\|\\[\\]\\{\\}:;\\\"\\',.<>\\/\\?]{8,32}$";
    /** connect id */
    public static final String CONNECT_ID = "^[a-zA-Z]{1}[_a-zA-Z0-9]{5,19}+$";
    /** expression */
    public static final String VERIFYCATION_EMOTION = "\\[([\\u4e00-\\u9fa5]|[a-zA-Z]|[0-9])+\\]";

    public static boolean matches(String value, String rule) {
        return value.matches(rule);
    }

    public static String[] splite(String value, String rule) {
        return value.split(rule);
    }

    public static String replace(String value, String oldstr, String newstr) {
        return value.replaceAll(oldstr, newstr);
    }

    public static String groupAvatar(String groupkey) {
        return String.format(RegularUtil.GROUP_AVATAR, XmlParser.getInstance().serverAddress(), groupkey);
    }
}
