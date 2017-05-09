package com.green;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Index;
import org.greenrobot.greendao.generator.PropertyType;
import org.greenrobot.greendao.generator.Schema;

public class GreenDb {

    private Schema schema;

    public static void main(String[] args) throws Exception {
        GreenDb greenDb = new GreenDb();
        greenDb.generate();
    }

    public GreenDb() {
        schema = new Schema(1, "connect.db.green.bean");
        //*******************  KEY-VALUE
        paramsInfo();

        //*******************  The contact book
        //The contact
        contactInfo();
        //group
        groupInfo();
        //Group members
        groupMemberInfo();
        //Recommend friends
        recommendFriendInfo();
        //friend request
        friendRequestInfo();

        //*******************  chat
        //chat
        conversionInfo();
        //Chat Settings
        conversionSettingInfo();
        //Message details
        messageInfo();
        //Trade table
        transactionInfo();
    }

    public void generate() throws Exception {
        DaoGenerator daoGenerator = new DaoGenerator();
        daoGenerator.generateAll(schema, "./greendb/db");
    }

    /**
     * Save the account key - value value
     */
    protected void paramsInfo() {
        Entity entity = schema.addEntity("ParamEntity");
        entity.addLongProperty("_id").primaryKey().autoincrement();
        entity.addStringProperty("key").unique();
        entity.addStringProperty("value");
        entity.addStringProperty("ext");
        entity.implementsSerializable();
    }

    /**
     * Recommend friends
     */
    protected void recommendFriendInfo() {
        Entity entity = schema.addEntity("RecommandFriendEntity");
        entity.addLongProperty("_id").primaryKey().autoincrement();
        entity.addStringProperty("pub_key").notNull().unique();
        entity.addStringProperty("username").notNull();
        entity.addStringProperty("address").notNull().unique();
        entity.addStringProperty("avatar").notNull();
        entity.addIntProperty("status");
        entity.implementsSerializable();
    }

    /**
     * friend requests
     */
    protected void friendRequestInfo() {
        Entity entity = schema.addEntity("FriendRequestEntity");
        entity.addLongProperty("_id").primaryKey().autoincrement();
        entity.addStringProperty("pub_key").notNull();
        entity.addStringProperty("address").notNull();
        entity.addStringProperty("avatar");
        entity.addStringProperty("username");
        entity.addStringProperty("tips");
        entity.addIntProperty("source");
        entity.addIntProperty("status");
        entity.addIntProperty("read");
        entity.implementsSerializable();
    }

    /**
     * contact friends
     */
    protected void contactInfo() {
        Entity entity = schema.addEntity("ContactEntity");
        entity.addLongProperty("_id").primaryKey().autoincrement();
        entity.addStringProperty("pub_key").notNull().unique();
        entity.addStringProperty("address").unique();
        entity.addStringProperty("username").notNull();
        entity.addStringProperty("avatar");
        entity.addStringProperty("remark");
        entity.addIntProperty("common");
        entity.addIntProperty("source");
        entity.addBooleanProperty("blocked");//The black
        entity.implementsSerializable();
    }

    /**
     * Create group
     */
    protected void groupInfo() {
        Entity entity = schema.addEntity("GroupEntity");
        entity.addLongProperty("_id").primaryKey().autoincrement();
        entity.addStringProperty("identifier").notNull().unique();
        entity.addStringProperty("name");
        entity.addStringProperty("ecdh_key");
        entity.addIntProperty("common");
        entity.addIntProperty("verify");//Group validation
        entity.addIntProperty("pub");//Group public
        entity.addStringProperty("avatar");//Group avatar
        entity.addStringProperty("summary");//Group introduce
        entity.implementsSerializable();
    }

    /**
     * Group member list
     */
    protected void groupMemberInfo() {
        Entity entity = schema.addEntity("GroupMemberEntity");
        entity.addLongProperty("_id").primaryKey().autoincrement();
        entity.addStringProperty("identifier").notNull();
        entity.addStringProperty("username").notNull();
        entity.addStringProperty("avatar").notNull();
        entity.addStringProperty("address").notNull();
        entity.addIntProperty("role");
        entity.addStringProperty("nick");
        entity.addStringProperty("pub_key");
        entity.implementsSerializable();
    }

    /**
     * Message transaction extension
     */
    protected void transactionInfo() {
        Entity entity = schema.addEntity("TransactionEntity");
        entity.addLongProperty("_id").primaryKey().autoincrement();
        entity.addStringProperty("message_id").notNull();
        entity.addStringProperty("hashid").notNull().unique();//A unique identifier of the deal
        //transfer
        entity.addIntProperty("status");//0:Did not pay 1:unconfirmed 2:confirmed
        //gather
        entity.addIntProperty("pay_count");//The number of people have to pay
        entity.addIntProperty("crowd_count");//The number of people invited to pay
        entity.implementsSerializable();
    }

    /**
     * The recent chat
     */
    protected void conversionInfo() {
        Entity entity = schema.addEntity("ConversionEntity");
        entity.addLongProperty("_id").primaryKey().autoincrement();
        entity.addStringProperty("identifier").unique();
        entity.addIntProperty("type");//0:private chat 1:group chat
        entity.addStringProperty("name");
        entity.addStringProperty("avatar");
        entity.addStringProperty("draft");
        entity.addStringProperty("content");
        entity.addIntProperty("unread_count");
        entity.addIntProperty("top");
        entity.addIntProperty("notice");//@
        entity.addIntProperty("stranger");//stranger 0:not stranger 1:is stranger
        entity.addLongProperty("last_time");
        entity.implementsSerializable();
    }

    /**
     * Chat message set
     * 0:close 1:open
     */
    protected void conversionSettingInfo() {
        Entity entity = schema.addEntity("ConversionSettingEntity");
        entity.addLongProperty("_id").primaryKey().autoincrement();
        entity.addStringProperty("identifier");
        entity.addLongProperty("snap_time");
        entity.addIntProperty("disturb");//The message disturb 0:close 1:open
        entity.implementsSerializable();
    }

    /**
     * Chat messages
     */
    protected void messageInfo() {
        Entity entity = schema.addEntity("MessageEntity");
        entity.addLongProperty("_id").primaryKey().autoincrement();
        entity.addStringProperty("message_ower").notNull();//public key
        entity.addStringProperty("message_id").notNull().unique();//message id
        entity.addStringProperty("content");//Encrypted gcmData(source:messageData.getCipherData())
        entity.addLongProperty("read_time");//The message has been read (mainly for voice)
        entity.addIntProperty("state");//0: not read 1: click but not read 2: readed (read the update time)
        entity.addIntProperty("send_status");//Message state 0: sending 1: send success/receive message 2: send failed
        entity.addLongProperty("snap_time");//start read time
        entity.addLongProperty("createtime");//show the send time
        entity.implementsSerializable();
    }
}
