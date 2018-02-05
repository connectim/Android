package instant.sender.model;

import java.io.Serializable;

import instant.bean.ChatMsgEntity;
import instant.bean.MessageType;

/**
 * Created by pujin on 2017/1/19.
 */

public abstract class BaseChat<T> implements Serializable {

    private static String TAG = "BaseChat";

    /**
     * 发送文本
     * @param string 消息文本
     * @return
     */
    public abstract T txtMsg(String string);

    /**
     * 发送图片
     * @param thum 缩略图地址
     * @param url 原图地址
     * @param filesize 图片大小
     * @param width 图片宽度
     * @param height 图片高度
     * @return
     */
    public abstract T photoMsg(String thum, String url, String filesize, int width, int height);

    /**
     * 发送音频
     * @param url  音频地址
     * @param length 音频长度 (单位:秒)
     * @return
     */
    public abstract T voiceMsg(String url, int length);

    /**
     * 发送视频
     * @param thum 缩略图地址
     * @param url 视频地址
     * @param length 视频长度
     * @param filesize 视频文件大小
     * @param width 视频宽度
     * @param height 视频高度
     * @return
     */
    public abstract T videoMsg(String thum,String url, int length,int filesize,int width,int height);

    /**
     * 发送表情
     * @param url 表情的名称
     * @return
     */
    public abstract T emotionMsg(String url);

    /**
     * 发送名片
     * @param pubkey 好友的公钥
     * @param name 好友名称
     * @param avatar 好友头像
     * @return
     */
    public abstract T cardMsg(String pubkey, String name, String avatar);

    /**
     * 发送位置
     * @param latitude 地理位置纬度
     * @param longitude 地理位置经度
     * @param address 地理位置描述地址
     * @param thum 位置缩略图
     * @param width 缩略图宽度
     * @param height 缩略图高度
     * @return
     */
    public abstract T locationMsg(float latitude,float longitude,String address,String thum,int width,int height);

    /**
     * @param noticeType 0: normal text
     *                   1:payment
     *                   2:crowding
     *                   3:lucky packet
     *                   4:stranger
     *                   5:审核信息
     *                   6:访客信息
     * @param content show text
     * @param ext ext
     * @return
     */
    public abstract T noticeMsg(int noticeType,String content,String ext);

    /**
     * @param type 0:private  1:group  2:outer
     * @param hashid
     * @param amout
     * @param tips
     * @return
     */
    public abstract T transferMsg(int type, String hashid, long amout, String tips);

    /**
     * @param type 0:inner 1:outer 2:system
     * @param hashid
     * @param tips
     * @param amount
     * @return
     */
    public abstract T luckPacketMsg(int type, String hashid, long amount,String tips);

    /**
     * @param paymenttype 0: private 1:crowding
     * @param hashid
     * @param amount
     * @param membersize
     * @param tips
     * @return
     */
    public abstract T paymentMsg(int paymenttype,String hashid, long amount, int membersize, String tips);

    /**
     * 发送外部链接
     * @param url 链接地址
     * @param title 标题
     * @param subtitle 副标题
     * @param img 图片
     * @return
     */
    public abstract T outerWebsiteMsg(String url, String title, String subtitle, String img);

    /**
     * 初始化不同类型的消息
     * @param type
     * @return
     */
    public abstract T createBaseChat(MessageType type);

    /**
     * 发送消息
     * @param chatMsgEntity
     */
    public abstract void sendPushMsg(ChatMsgEntity chatMsgEntity);

    public abstract String chatKey();

    public abstract int chatType();

    public abstract String headImg();

    public abstract String nickName();

    public abstract String friendPublicKey();
}
