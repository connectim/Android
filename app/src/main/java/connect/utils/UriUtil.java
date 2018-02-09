package connect.utils;

/**
 * http request URI
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
    /** Verify the authentication code are legal */
    public static String CONNECT_V2_SMS_VALIDATE = "/connect/v2/sms/validate";
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
    /** The private key is registered */
    public static String CONNECT_V2_SIGN_IN_CA = "/connect/v2/sign_in/ca";
    /** The private key is registered */
    public static String CONNECT_V2_SIGN_UP = "/connect/v2/sign_up";
    /** The private key is registered */
    public static String CONNECT_V2_SIGN_UP_PASSWORD = "/connect/v2/sign_in/password";
    /** The private key is registered */
    public static String CONNECT_V3_LOGIN = "/connect/v3/login";
    /** The private key is registered */
    public static String CONNECT_V3_SYNC_WORKMATE = "/connect/v3/sync_workmate";
    /** The private key is registered */
    public static String CONNECT_V3_DEPARTMENT = "/connect/v3/department";
    /** Query the user information 1: name(en)  2:uid  3:name(zh)*/
    public static String CONNECT_V3_WORKMATE_SEARCH = "/connect/v3/workmate/search";
    public static String CONNECT_V3_USERS_FOLLOW = "/connect/v3/users/follow";
    public static String CONNECT_V3_PROXY_TOKEN = "/visitors/v1/staff/token";
    public static String CONNECT_V3_API_BANNERS = "/connect/v3/api/banners";
    public static String CONNECT_V3_API_APPLICATIONS = "/connect/v3/api/applications";
    public static String CONNECT_V3_PROXY_VISITOR_RECORDS = "/visitors/v1/staff/records";
    public static String CONNECT_V3_PROXY_EXAMINE_VERIFY = "/visitors/v1/staff/records/examine/verify";
    public static String CONNECT_V3_PROXY_RECORDS_HISTORY = "/visitors/v1/staff/records/history";

    public static String CONNECT_V3_API_APPLICATIONS_ADD = "/connect/v3/api/applications/add";
    public static String CONNECT_V3_API_APPLICATIONS_DEL = "/connect/v3/api/applications/del";

    /** 根据 部门id查询 所有员工 */
    public static String CONNECT_V3_DEPAERTMENT_WORKMATES = "/connect/v3/department/workmates";
    /** 群成员邀请入群 */
    public static String CONNECT_V3_GROUP_INVITE = "/connect/v3/group/invite";
    /** 获取仓库列表 */
    public static String STORES_V1_IWORK_LOGS = "/stores/v1/iwork/logs";
    /** 确认陌生人信息 */
    public static String STORES_V1_IWORK_LOG_COMFIRM = "/stores/v1/iwork/log/confirm";
    /** 查询陌生人信息详情 */
    public static String STORES_V1_IWORK_LOGS_DETAIL = "/stores/v1/iwork/logs/detail";

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
    /** Query the user information 1: uid   2:connectid  "":username*/
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
    /** 钱包设置信息 */
    public static String SETTING_PAY_SUNC = "/connect/v1/setting/pay/setting/sync";
    /** pay setting */
    public static String SETTING_PAY_SETTING = "/connect/v1/setting/pay/setting";
    /** 钱包版本号 */
    public static String SETTING_PAY_VERSION = "/connect/v1/setting/pay/pin/version";
    /** pay password setting */
    public static String SETTING_PAY_PIN_SETTING = "/connect/v1/setting/pay/pin/setting";
    /** Send a message authentication code */
    public static String V2_SMS_SEND = "/connect/v2/sms/send";
    /** Modify the second authentication password */
    public static String V2_SRTTING_PASSWORD_UPDATE = "/connect/v2/setting/password/update";
    /** Verify phone verification code */
    public static String V2_SETTING_MOBILE_VERIFY = "/connect/v2/setting/mobile/verify";
    /** delete account */
    public static String V2_SETTING_DELETE_USER = "/connect/v2/setting/delete_user";
    /** delete account */
    public static String CONNECT_V3_PUBKEY = "/connect/v3/pubkey";

    /**======================================================================================
     *                                wallet
     * ====================================================================================== */
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
    /** create wallet */
    public static String WALLET_V2_CREATE = "/wallet/v2/create";
    /** sync wallet */
    public static String WALLET_V2_SYNC = "/wallet/v2/sync";
    /** create coin */
    public static String WALLET_V2_COINS_CREATE = "/wallet/v2/coins/create";
    /** update wallet */
    public static String WALLET_V2_UPDATA = "/wallet/v2/update";
    /** coin list */
    public static String WALLET_V2_COINS_LIST = "/wallet/v2/coins/list";
    /** update coin */
    public static String WALLET_V2_COINS_UPDATA = "/wallet/v2/coins/update";
    /** address default */
    public static String WALLET_V2_COINS_ADDRESS_DEFAULT = "/wallet/v2/coins/addresses/default";
    /** update coin */
    public static String WALLET_V2_COINS_ADDRESS_GET_DEFAULT = "/wallet/v2/coins/addresses/get_default";
    /** service transfer */
    public static String WALLET_V2_SERVICE_TRANSFER = "/wallet/v2/service/transfer";
    /** coins addresses */
    public static String WALLET_V2_COINS_ADDRESS_LIST = "/wallet/v2/coins/addresses/list";
    /** Get currency status of purse */
    public static String WALLET_V2_SERVICE_USER_STATUS = "/wallet/v2/service/user_status";
    /** Collection (not part of purse basic function)*/
    public static String WALLET_V2_SERVICE_RECEIVE = "/wallet/v2/service/receive";
    /** payment */
    public static String WALLET_V2_SERVICE_PAYMENT = "/wallet/v2/service/payment";
    /** Redpacket */
    public static String WALLET_V2_SERVICE_LUCKPACKAGE = "/wallet/v2/service/luckpackage";
    /** external transfer */
    public static String WALLET_V2_SERVICE_EXTERNAL = "/wallet/v2/service/external";
    /** Crowd-funding */
    public static String WALLET_V2_SERVICE_CROWDFUNING = "/wallet/v2/service/crowdfuning";
    /** Radio broadcast */
    public static String WALLET_V2_SERVICE_PUBLISH = "/wallet/v2/service/publish";
    /** Transfer to address */
    public static String WALLET_V2_SERVICE_TRANSFER_ADDRESS= "/wallet/v2/service/transfer_to_address";
    /** Get address transaction stream */
    public static String WALLET_V2_COINS_ADDRESSES_TX = "/wallet/v2/coins/addresses/tx";
    /** Get currency information */
    public static String WALLET_V2_COINS_INFO = "/wallet/v2/coins/info";


    /**======================================================================================
     *                                contact
     * ====================================================================================== */
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
    /** Get users by search public key */
    public static String CONNEXT_V1_USERS_SEARCHBYPUBKEY = "/connect/v1/users/searchByPubKey";
    /** Get users by search public key */
    public static String CONNEXT_V1_USERS_DISINCLINE = "/connect/v1/users/disincline";
    public static String CONNECT_V2_RSS = "/connect/v2/rss";
    public static String CONNECT_V2_RSS_FOLLOW = "/connect/v2/rss/follow";
    public static String CONNECT_V2_MARKET_EXCHANGE = "/connect/v2/market/exchange";
    public static String CONNECT_V2_MARKET_ID= "/connect/v2/market/exchange/%s";
    public static String CONNECT_V2_MARKET_CAPITALIZATIONS = "/connect/v2/market/capitalizations";

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
