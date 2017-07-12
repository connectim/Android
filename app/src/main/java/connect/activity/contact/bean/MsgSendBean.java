package connect.activity.contact.bean;

import java.io.Serializable;

/**
 * send message success,back Value
 * Created by Administrator on 2017/2/4.
 */

public class MsgSendBean implements Serializable {

    private String username;
    private String avatar;
    private String pubkey;
    private String address;
    private Boolean common;
    private Integer source;
    private String remark;
    private Boolean block;
    private SendType type;
    private String tips;

    public enum SendType{
        TypeSendFriendQuest,//add friend
        TypeAcceptFriendQuest,//receive friend
        TypeDeleteFriend,//remove friend
        TypeAddFavorites,//add command friend
        TypeFriendRemark,//friend nickname
        TypeRecommendNoInterested,//Recommend friends are not interested in
        TypeOutPacket,//outer lucky pakcet
        TypeOutTransfer,//outer transfer
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPubkey() {
        return pubkey;
    }

    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getCommon() {
        return common;
    }

    public void setCommon(Boolean common) {
        this.common = common;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Boolean getBlock() {
        return block;
    }

    public void setBlock(Boolean block) {
        this.block = block;
    }

    public SendType getType() {
        return type;
    }

    public void setType(SendType type) {
        this.type = type;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}
