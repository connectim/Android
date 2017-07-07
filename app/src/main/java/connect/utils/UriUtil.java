package connect.utils;

/**
 * http request URI
 * Created by gtq on 2016/11/21.
 */
public class UriUtil {

    /**======================================================================================
     *                                Login
     * ====================================================================================== */
    /** Request to send SMS verification code */
    public static String LAUNCH_IMAGES = "/launch_images/v1/%s/images";
    /** Request to send a text message authentication code */
    public static String CONNECT_V1_SMS_SEND = "/connect/v1/sms/send";
    /** Verify the authentication code are legal */
    public static String CONNECT_V1_SIGN_IN = "/connect/v1/sign_in";
    /** Upload users avatar */
    public static String AVATAR_V1_UP = "/avatar/v1/up";
    /** registered */
    public static String CONNECT_V1_SIGN_UP = "/connect/v1/sign_up";
    /** Upload a file */
    public static String UPLOAD_FILE = "/fs/v1/up";
    /** To test whether the private key registration */
    public static String CONNECT_V1_PRIVATE_EXISTED = "/connect/v1/private/user_existed";
    /** The private key is registered */
    public static String CONNECT_V1_PRIVATE_SIGN = "/connect/v1/private/sign_in";

    /**======================================================================================
     *                                Login successfully initialized
     * ====================================================================================== */
    /** The key to expand */
    public static String CONNECT_USERS_EXPIRE_SALT = "/connect/v1/users/expire/salt";
    /** The key to expand */
    public static String CONNECT_USER_SALT = "/connect/v1/users/salt";
    /** The App update */
    public static String CONNECT_V1_VERSION = "/connect/v1/version";


    /**======================================================================================
     *                                setting
     * ====================================================================================== */
    /** Query the user information */
    public static String CONNECT_V1_USER_SEARCH = "/connect/v1/users/search";
    /** Set user basic information */
    public static String CONNECT_V1_SETTING_USERINFO = "/connect/v1/setting/userinfo";
    /** set ConnectId */
    public static String CONNECT_V1_SETTING_CONNECTID = "/connect/v1/setting/connectId";
    /** set user avatar */
    public static String AVATAR_V1_SET = "/connect/v1/setting/avatar";
    /** bind mobile */
    public static String SETTING_BIND_MOBILE = "/connect/v1/setting/bind/mobile";
    /** unbind mobile */
    public static String SETTING_UNBIND_MOBILE = "/connect/v1/setting/unbind/mobile";
    /** set new password */
    public static String SETTING_BACK_KEY = "/connect/v1/setting/backup/key";
    /** get private setting */
    public static String SETTING_PRIVACY_INFO = "/connect/v1/setting/privacy/info";
    /** sync contact */
    public static String SETTING_PHONE_SYNC = "/connect/v1/setting/phonebook/sync";
    /** Settings are recommended */
    public static String SETTING_SETTING_RECOMMEND = "/connect/v1/setting/recommend";
    /** private setting */
    public static String SETTING_PRIVACY = "/connect/v1/setting/privacy";
    /** sync pay setting */
    public static String SETTING_PAY_SUNC = "/connect/v1/setting/pay/setting/sync";
    /** pay setting */
    public static String SETTING_PAY_SETTING = "/connect/v1/setting/pay/setting";
    /** Pay the password version */
    public static String SETTING_PAY_VERSION = "/connect/v1/setting/pay/pin/version";
    /** pay password setting */
    public static String SETTING_PAY_PIN_SETTING = "/connect/v1/setting/pay/pin/setting";


    /**======================================================================================
     *                                wallet
     * ====================================================================================== */
    /** Check account balances */
    public static String BLOCKCHAIN_UNSPENT_INFO = "/blockchain/v1/unspent/%s/info";
    /** Query the address all of the transactions */
    public static String BLOCKCHAIN_ADDRESS_TX = "/blockchain/v1/address/%s/tx?page=%d&pagesize=%d";
    /** Fundamental information for sending a red envelope */
    public static String WALLET_PACKAGE_PENDING = "/wallet/v1/red_package/pending";
    /** Check the address did not spend trading */
    public static String BLOCKCHAIN_UNSPENT_OEDER = "/blockchain/v1/unspent/%s/order";
    /** Estimate to use (for once a day) */
    public static String CONNECT_V1_ESTIMATEFEE = "/connect/v1/estimatefee";
    /** Sending lucky packet */
    public static String WALLET_PACKAGE_SEND = "/wallet/v1/red_package/send";
    /** lucky packet history */
    public static String WALLET_PACKAGE_HOSTORY = "/wallet/v1/red_package/history";
    /** lucky packet detail */
    public static String WALLET_PACKAGE_INFO = "/wallet/v1/red_package/info";
    /** For the middle of the external transfer address */
    public static String WALLET_EXTERNAL_PENDING = "/wallet/v1/billing/external/pending";
    /** Send the outer transfer */
    public static String WALLET_BILLING_EXTERNAL_SEND = "/wallet/v1/billing/external/send";
    /** Withdrawal of external transfer */
    public static String WALLET_BILLING_EXTERNAL_CANCLE = "/wallet/v1/billing/external/cancel";
    /** External transfer history */
    public static String WALLET_BILLING_EXTERNAL_HISTORY = "/wallet/v1/billing/external/history";
    /** Transfer to personal */
    public static String WALLET_BILLING_SEND = "/wallet/v1/billing/send";
    /** Broadcasting deals */
    public static String WALLET_BILLING_PUBLISH_TX = "/wallet/v1/billing/publish/tx";
    /** Access to address this */
    public static String WALLET_ADDRESS_BOOK_LIST = "/wallet/v1/address_book/list";
    /** Add the address */
    public static String WALLET_ADDRESS_BOOK_ADD = "/wallet/v1/address_book/add";
    /** add Tag */
    public static String WALLET_ADDRESS_BOOK_TAG = "/wallet/v1/address_book/tag";
    /** delete address */
    public static String WALLET_ADDRESS_BOOK_REMOVE = "/wallet/v1/address_book/remove";
    /** Many people transfer */
    public static String WALLET_BILLING_MUILT_SEND = "/wallet/v1/billing/muilt_send";

    /**======================================================================================
     *                                contact
     * ====================================================================================== */
    /** add in blacklist */
    public static String CONNEXT_V1_BLACKLIST = "/connect/v1/blacklist/";
    /** remove from blacklist */
    public static String CONNEXT_V1_BLACKLIST_REMOVE = "/connect/v1/blacklist/remove";
    /** get blacklist */
    public static String CONNEXT_V1_BLACKLIST_LIST = "/connect/v1/blacklist/list";
    /** sync friend */
    public static String CONNEXT_V1_USERS_PHONEBOOK = "/connect/v1/users/phonebook";
    /** People may know */
    public static String CONNEXT_V1_USERS_RECOMMEND = "/connect/v1/users/recommend";
    /** Get friends transfer record */
    public static String CONNEXT_V1_USERS_FRIEND_RECORDS = "/connect/v1/users/friends/records";

    /**======================================================================================
     *                                      setting group
     * ======================================================================================
     */
    /** create group */
    public static String CREATE_GROUP = "/connect/v1/group";
    /** Invited into the group */
    public static String GROUP_ADDUSER = "/connect/v1/group/adduser";
    /** remove member */
    public static String GROUP_REMOVE = "/connect/v1/group/deluser";
    /** exit from group */
    public static String GROUP_QUIT = "/connect/v1/group/quit";
    /** command group */
    public static String GROUP_COMMON = "/connect/v1/group/set_common";
    /** remove command */
    public static String GROUP_RECOMMON = "/connect/v1/group/remove_common";
    /** modify member nick */
    public static String GROUP_MEMUPDATE = "/connect/v1/group/member_update";
    /** modify group nick */
    public static String GROUP_UPDATE = "/connect/v1/group/update";
    /** get group detail */
    public static String GROUP_PULLINFO = "/connect/v1/group/info";
    /** get group setting */
    public static String GROUP_SETTING_INFO = "/connect/v1/group/setting_info";
    /** group qrcode hash */
    public static String GROUP_HASH = "/connect/v1/group/hash";
    /** update group qrcode hash */
    public static String GROUP_REFRESH_HASH = "/connect/v1/group/refresh/hash";
    /** Group manager change */
    public static String GROUP_ATTORN = "/connect/v1/group/attorn";
    /** group setting */
    public static String GROUP_SETTING = "/connect/v1/group/setting";
    /** Group public information */
    public static String GROUP_PUBLIC_INFO = "/connect/v1/group/public_info";
    /** Group members invite bid to join the group*/
    public static String GROUP_INVITE = "/connect/v1/group/invite";
    /** Apply to the group of */
    public static String GROUP_APPLY = "/connect/v1/group/apply";
    /** Agree to apply for */
    public static String GROUP_REVIEWED= "/connect/v1/group/reviewed";
    /** Refuse to apply for the group */
    public static String GROUP_REJECT = "/connect/v1/group/reject";
    /** Upload groups face */
    public static String GROUP_AVATAR = "/connect/v1/group/avatar";
    /** Single initiate payment */
    public static String BILLING_RECIVE = "/wallet/v1/billing/recive";
    /** Group a collection */
    public static String CROWDFUN_LAUNCH = "/wallet/v1/crowdfuning/launch";
    /** Group gathering records */
    public static String CROWDFUN_RECORDS = "/wallet/v1/crowdfuning/records/users";
    /** Transfer detail  inner */
    public static String TRANSFER_INNER = "/wallet/v1/billing/info";
    /** Transfer details outer */
    public static String TRANSFER_OUTER = "/wallet/v1/billing/external/info";
    /** Transfer details private */
    public static String BILLING_INFO = "/wallet/v1/billing/info";
    /** gather details group */
    public static String CROWDFUN_INFO = "/wallet/v1/crowdfuning/info";
    /** gather pay */
    public static String CROWDFUN_PAY = "/wallet/v1/crowdfuning/pay";
    /** request lucky packet */
    public static String REDPACKAGE_GRAB = "/wallet/v1/red_package/grab";
    /** upload group backup */
    public static String CONNECT_GROUP_UPLOADKEY = "/connect/v1/group/upload_key";
    /** download group backup by yourself */
    public static String CONNECT_GROUP_DOWNLOAD_KEY = "/connect/v1/group/download_key";
    /** download group backup by group */
    public static String CONNECT_GROUP_BACKUP = "/connect/v1/group/backup";
    /** share group qrcode */
    public static String CONNECT_GROUP_SHARE = "/connect/v1/group/share";
    /** group TOKEN */
    public static String CONNECT_GROUP_INFOTOKEN = "/connect/v1/group/info/token";
    /** group invite token */
    public static String GROUP_INVITE_TOKEN = "/connect/v1/group/invite/token";
    /** group mute */
    public static String CONNECT_GROUP_MUTE = "/connect/v1/group/mute";

    /** get system lucky packet */
    public static String WALLET_PACKAGE_GRABSYSTEM = "/wallet/v1/red_package/grabSystem";
    /** system lucky packet detail */
    public static String WALLET_PACKAGE_SYSTEMINFO = "/wallet/v1/red_package/system/info";
}
