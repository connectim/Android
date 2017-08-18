package connect.database.green.DaoHelper.mergin;

import android.database.Cursor;
import android.database.SQLException;

import com.google.gson.Gson;
import com.google.protobuf.GeneratedMessageV3;

import org.greenrobot.greendao.database.Database;

import java.io.Serializable;

import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.GeoAddressBean;
import connect.database.MemoryDataManager;
import connect.database.green.dao.MessageEntityDao;
import connect.database.green.dao.RecommandFriendEntityDao;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.utils.StringUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.log.LogManager;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/17.
 */

public class MigrateVersionFourHelper extends MigrateVerisonHelper {

    private String Tag = "MigrateVersionFourHelper";
    private Database database;

    private String tableName = MessageEntityDao.TABLENAME;
    private String tempTableName = tableName + "_TEMP";

    public MigrateVersionFourHelper(Database database) {
        this.database = database;
    }

    @Override
    public void migrate() {
        try {
            createTempTable();
            migrateMessage(database);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createTempTable() {
        StringBuilder insertTableStringBuilder = new StringBuilder();

        try {
            if (MigrationHelper.isTableExists(database, false, tempTableName)) {
                return;
            }

            //Create a temporary table
            insertTableStringBuilder.append("CREATE TEMPORARY TABLE ").append(tempTableName);
            insertTableStringBuilder.append(" AS SELECT * FROM ").append(tableName).append(";");
            database.execSQL(insertTableStringBuilder.toString());

            MessageEntityDao.dropTable(database, true);
            MessageEntityDao.createTable(database, true);
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to generate temp table]" + tempTableName);
        }
    }

    public void migrateMessage(Database db) throws Exception {
        String tableName = MessageEntityDao.TABLENAME;
        byte[] localHashKeys = SupportKeyUril.localHashKey().getBytes();

        try {
            Cursor cursor = db.rawQuery("SELECT * FROM " + tempTableName, new String[]{});
            while (cursor.moveToNext()) {
                String messageOwner = cursorGetString(cursor, "MESSAGE_OWER");
                String messageId = cursorGetString(cursor, "MESSAGE_ID");
                String tempContent = cursorGetString(cursor, "CONTENT");
                long readTime = cursorGetLong(cursor, "READ_TIME");
                int state = cursorGetInt(cursor, "STATE");
                int sendStatus = cursorGetInt(cursor, "SEND_STATUS");
                long snapTime = cursorGetLong(cursor, "SNAP_TIME");
                long createTime = cursorGetLong(cursor, "CREATETIME");

                Connect.GcmData gcmData = Connect.GcmData.parseFrom(StringUtil.hexStringToBytes(tempContent));
                byte[] contents = DecryptionUtil.decodeAESGCM(SupportKeyUril.EcdhExts.NONE, localHashKeys, gcmData);
                MsgDefinBean definBean = new Gson().fromJson(new String(contents), MsgDefinBean.class);


                String from = "";
                String to = "";
                String content = "";

                String connect = BaseApplication.getInstance().getString(R.string.app_name);
                Connect.ChatType chatType = messageOwner.equals(connect) ? Connect.ChatType.CONNECT_SYSTEM :
                        (definBean.getUser_id().length() == 64 ? Connect.ChatType.GROUPCHAT :
                                Connect.ChatType.PRIVATE);

                String myPublickKey = MemoryDataManager.getInstance().getPubKey();
                MsgSender msgSender = definBean.getSenderInfoExt();
                if (definBean.getSenderInfoExt() == null) {
                    from = myPublickKey;
                    to = connect;
                } else {
                    String senderPublicKey = msgSender.getPublickey();
                    if (myPublickKey.equals(msgSender.getPublickey())) {
                        from = myPublickKey;
                        to = senderPublicKey;
                    } else {
                        from = senderPublicKey;
                        to = myPublickKey;
                    }
                }

                MsgType msgType = MsgType.toMsgType(definBean.getType());
                GeneratedMessageV3 messageV3 = null;
                switch (msgType) {
                    case Text:
                        messageV3 = Connect.TextMessage.newBuilder()
                                .setContent(definBean.getContent()).build();
                        break;
                    case Emotion:
                        messageV3 = Connect.EmotionMessage.newBuilder()
                                .setContent(definBean.getContent()).build();
                        break;
                    case Photo:
                        messageV3 = Connect.PhotoMessage.newBuilder()
                                .setThum(definBean.getContent())
                                .setUrl(definBean.getUrl())
                                .setImageWidth((int) definBean.getImageOriginWidth())
                                .setImageHeight((int) definBean.getImageOriginHeight())
                                .setSize(definBean.getExt1()).build();
                        break;
                    case Voice:
                        messageV3 = Connect.VoiceMessage.newBuilder()
                                .setUrl(definBean.getContent())
                                .setTimeLength(definBean.getSize()).build();
                        break;
                    case Video:
                        messageV3 = Connect.VideoMessage.newBuilder()
                                .setCover(definBean.getContent())
                                .setUrl(definBean.getUrl())
                                .setImageWidth((int) definBean.getImageOriginWidth())
                                .setImageHeight((int) definBean.getImageOriginHeight())
                                .setTimeLength(definBean.getSize())
                                .setSize(300).build();
                        break;
                    case Name_Card:
                        CardExt1Bean cardExt1Bean = new Gson().fromJson(definBean.getExt1(), CardExt1Bean.class);
                        messageV3 = Connect.CardMessage.newBuilder()
                                .setAvatar(cardExt1Bean.getAvatar())
                                .setUid(cardExt1Bean.getPub_key())
                                .setUsername(cardExt1Bean.getUsername()).build();
                        break;
                    case Self_destruct_Notice:
                        messageV3 = Connect.DestructMessage.newBuilder()
                                .setTime((int) Long.parseLong(definBean.getContent())).build();
                        break;
                    case Self_destruct_Receipt:
                        messageV3 = Connect.ReadReceiptMessage.newBuilder()
                                .setMessageId(definBean.getContent()).build();
                        break;
                    case Request_Payment:
                        GatherBean gatherBean = new Gson().fromJson(definBean.getExt1(), GatherBean.class);
                        int paymentType = chatType == Connect.ChatType.PRIVATE ? 0 : 1;

                        messageV3 = Connect.PaymentMessage.newBuilder()
                                .setPaymentType(paymentType)
                                .setHashId(gatherBean.getHashid())
                                .setAmount(gatherBean.getAmount())
                                .setMemberSize(gatherBean.getTotalMember())
                                .setTips(gatherBean.getNote()).build();
                        break;
                    case Transfer:
                        TransferExt transferExt = new Gson().fromJson(definBean.getExt1(), TransferExt.class);

                        messageV3 = Connect.TransferMessage.newBuilder()
                                .setHashId(definBean.getContent())
                                .setAmount(transferExt.getAmount())
                                .setTransferType(transferExt.getType())
                                .setTips(transferExt.getNote()).build();
                        break;
                    case Lucky_Packet:
                        TransferExt luckyExt = new Gson().fromJson(definBean.getExt1(), TransferExt.class);

                        messageV3 = Connect.LuckPacketMessage.newBuilder()
                                .setHashId(definBean.getContent())
                                .setLuckyType(luckyExt.getType())
                                .setAmount(luckyExt.getAmount())
                                .setTips(luckyExt.getNote()).build();
                        break;
                    case Location:
                        GeoAddressBean addressBean = definBean.getLocationExt();

                        messageV3 = Connect.LocationMessage.newBuilder()
                                .setAddress(addressBean.getAddress())
                                .setScreenShot(addressBean.getPath())
                                .setLatitude((float) addressBean.getLocationLatitude())
                                .setLongitude((float) addressBean.getLocationLongitude())
                                .setImageWidth((int) definBean.getImageOriginWidth())
                                .setImageHeight((int) definBean.getImageOriginHeight()).build();
                        break;
                    case NOTICE:
                        messageV3 = Connect.NotifyMessage.newBuilder()
                                .setContent(definBean.getContent()).build();
                        break;
                    case INVITE_GROUP:
                        GroupExt1Bean groupExt1Bean = new Gson().fromJson(definBean.getExt1(), GroupExt1Bean.class);

                        messageV3 = Connect.JoinGroupMessage.newBuilder()
                                .setAvatar(groupExt1Bean.getAvatar())
                                .setGroupName(groupExt1Bean.getGroupname())
                                .setGroupId(groupExt1Bean.getGroupidentifier())
                                .setToken(groupExt1Bean.getInviteToken()).build();
                        break;
                    case OUTER_WEBSITE:
                        WebsiteExt1Bean websiteExt1Bean = new Gson().fromJson(definBean.getExt1(), WebsiteExt1Bean.class);

                        messageV3 = Connect.WebsiteMessage.newBuilder()
                                .setUrl(definBean.getContent())
                                .setTitle(websiteExt1Bean.getLinkTitle())
                                .setSubtitle(websiteExt1Bean.getLinkSubtitle())
                                .setImg(websiteExt1Bean.getLinkImg()).build();
                        break;
                }

                if (messageV3 != null) {
                    content = encryptMessageByteArray(messageV3.toByteArray());
                    String sqlSyntax = "INSERT INTO " + tableName + " (MESSAGE_OWER,MESSAGE_ID,CHAT_TYPE,'MESSAGE_FROM','MESSAGE_TO',MESSAGE_TYPE,CONTENT,READ_TIME,SEND_STATUS,SNAP_TIME,CREATETIME) " +
                            "VALUES ( '" +
                            messageOwner + "' , '" + messageId + "' , " + chatType.getNumber() + " , '" + from + "' , '" + to + "' , " + msgType.type + " , '" +
                            content + "' , " + readTime + " , " + sendStatus + " , " + snapTime + " , " + createTime +
                            ");";
                    db.execSQL(sqlSyntax);
                }
            }
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to migrateMessage] " + e.getMessage());
        }
    }

    public String encryptMessageByteArray(byte[] messageByteArray) {
        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCM(SupportKeyUril.EcdhExts.NONE, SupportKeyUril.localHashKey().getBytes(), messageByteArray);
        return StringUtil.bytesToHexString(gcmData.toByteArray());
    }

    class MsgDefinBean implements Serializable {

        private int type;
        private String user_name;
        private long sendtime;
        private String message_id;
        private String publicKey;
        private String user_id;
        private String ext;
        private String content;
        private String url;
        private String ext1;
        private GeoAddressBean locationExt;
        private int size;
        private float imageOriginWidth;
        private float imageOriginHeight;
        private MsgSender senderInfoExt;

        public int getType() {
            return type;
        }

        public String getUser_name() {
            return user_name;
        }

        public long getSendtime() {
            return sendtime;
        }

        public String getMessage_id() {
            return message_id;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public String getUser_id() {
            return user_id;
        }

        public String getExt() {
            return ext;
        }

        public String getContent() {
            return content;
        }

        public String getUrl() {
            return url;
        }

        public String getExt1() {
            return ext1;
        }

        public GeoAddressBean getLocationExt() {
            return locationExt;
        }

        public int getSize() {
            return size;
        }

        public float getImageOriginWidth() {
            return imageOriginWidth;
        }

        public float getImageOriginHeight() {
            return imageOriginHeight;
        }

        public MsgSender getSenderInfoExt() {
            return senderInfoExt;
        }
    }

    class CardExt1Bean implements Serializable {

        private String username;
        private String avatar;
        private String pub_key;
        private String address;

        public String getUsername() {
            return username;
        }

        public String getAvatar() {
            return avatar;
        }

        public String getPub_key() {
            return pub_key;
        }

        public String getAddress() {
            return address;
        }
    }

    public class GatherBean implements Serializable {

        private String hashid;
        private long amount;
        private int totalMember;
        private boolean isCrowdfundRceipt;
        private String note;

        public String getHashid() {
            return hashid;
        }

        public long getAmount() {
            return amount;
        }

        public int getTotalMember() {
            return totalMember;
        }

        public boolean isCrowdfundRceipt() {
            return isCrowdfundRceipt;
        }

        public String getNote() {
            return note;
        }
    }

    public class TransferExt {

        private long amount;
        private String note;
        private int type;

        public long getAmount() {
            return amount;
        }

        public String getNote() {
            return note;
        }

        public int getType() {
            return type;
        }
    }

    public class GroupExt1Bean implements Serializable {

        private String avatar;
        private String groupname;
        private String groupidentifier;
        private String inviteToken;

        public String getAvatar() {
            return avatar;
        }

        public String getGroupname() {
            return groupname;
        }

        public String getGroupidentifier() {
            return groupidentifier;
        }

        public String getInviteToken() {
            return inviteToken;
        }
    }

    class WebsiteExt1Bean implements Serializable {

        private String linkTitle;
        private String linkSubtitle;
        private String linkImg;

        public String getLinkTitle() {
            return linkTitle;
        }

        public String getLinkSubtitle() {
            return linkSubtitle;
        }

        public String getLinkImg() {
            return linkImg;
        }
    }

    class MsgSender implements Serializable {

        public String publickey;
        public String username;
        public String address;
        public String avatar;

        public String getPublickey() {
            return publickey;
        }

        public String getUsername() {
            return username;
        }

        public String getAddress() {
            return address;
        }

        public String getAvatar() {
            return avatar;
        }
    }
}
