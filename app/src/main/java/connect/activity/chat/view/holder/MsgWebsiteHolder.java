package connect.activity.chat.view.holder;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import connect.activity.chat.exts.OuterWebsiteActivity;
import connect.activity.chat.exts.TransferToActivity;
import connect.ui.activity.R;
import connect.utils.ProtoBufUtil;
import connect.utils.RegularUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import instant.bean.ChatMsgEntity;
import instant.bean.UserOrderBean;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by pujin on 2017/2/20.
 */
public class MsgWebsiteHolder extends MsgChatHolder {

    private TextView titleTxt;
    private TextView contentTxt;
    private ImageView typeImg;

    public MsgWebsiteHolder(View itemView) {
        super(itemView);
        titleTxt = (TextView) itemView.findViewById(R.id.txt2);
        contentTxt = (TextView) itemView.findViewById(R.id.txt3);
        typeImg = (ImageView) itemView.findViewById(R.id.roundimg1);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final ChatMsgEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        Connect.WebsiteMessage websiteMessage = Connect.WebsiteMessage.parseFrom(msgExtEntity.getContents());

        titleTxt.setText(websiteMessage.getTitle());
        contentTxt.setText(websiteMessage.getSubtitle());

        String content = websiteMessage.getUrl();
        if (RegularUtil.matches(content, RegularUtil.OUTER_BITWEBSITE)) {
            if (RegularUtil.matches(content, RegularUtil.OUTER_BITWEBSITE_TRANSFER)) {//outer transfer
                GlideUtil.loadAvatarRound(typeImg, R.mipmap.message_send_bitcoin2x);
            } else if (RegularUtil.matches(content, RegularUtil.OUTER_BITWEBSITE_PACKET)) {//outer lucky packet
                GlideUtil.loadAvatarRound(typeImg, R.mipmap.luckybag3x);
            } else if (RegularUtil.matches(content, RegularUtil.OUTER_BITWEBSITE_PAY)) {//outer gather
                GlideUtil.loadAvatarRound(typeImg, R.mipmap.message_send_payment2x);
            }
        } else {//outer link
            String imgUrl = TextUtils.isEmpty(websiteMessage.getImg()) ? "" : websiteMessage.getImg();
            if (TextUtils.isEmpty(imgUrl)) {
                GlideUtil.loadAvatarRound(typeImg, R.mipmap.message_link2x);
            } else {
                GlideUtil.loadImage(typeImg, imgUrl,R.mipmap.message_link2x);
            }
        }

        contentLayout.setTag(content);
        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = (String) v.getTag();
                if (RegularUtil.matches(content, RegularUtil.OUTER_BITWEBSITE_TRANSFER)) {//outer transfer
                    GlideUtil.loadAvatarRound(typeImg, R.mipmap.message_send_bitcoin2x);

                    int transStart = content.indexOf("token") + 6;
                    int transEnd = content.indexOf("&");
                    if (transEnd == -1) {
                        transEnd = content.length();
                    }
                    String transToken = content.substring(transStart, transEnd);

                    UserOrderBean userOrderBean = new UserOrderBean();
                    userOrderBean.outerTransfer(transToken);
                } else if (RegularUtil.matches(content, RegularUtil.OUTER_BITWEBSITE_PACKET)) {//outer lucky packet
                    GlideUtil.loadAvatarRound(typeImg, R.mipmap.luckybag3x);

                    int packetStart = content.indexOf("token") + 6;
                    int packetEnd = content.indexOf("&");
                    if (packetEnd == -1) {
                        packetEnd = content.length();
                    }
                    String packetToken = content.substring(packetStart, packetEnd);
                    avaliableOuterRedPacket(packetToken);
                } else if (RegularUtil.matches(content, RegularUtil.OUTER_BITWEBSITE_PAY)) {//outer gather
                    GlideUtil.loadImage(typeImg, R.mipmap.message_send_payment2x);

                    int payStart = content.indexOf("address") + 8;
                    int payEnd = content.indexOf("&");
                    if (payEnd == -1) {
                        payEnd = content.length();
                    }
                    String transToken = content.substring(payStart, payEnd);
                    TransferToActivity.startActivity((Activity) context, transToken);
                } else {//outer link
                    OuterWebsiteActivity.startActivity((Activity) context, content);
                }
            }
        });
    }

    /**
     * token query detail
     *
     * @param token
     */
    private void avaliableOuterRedPacket(final String token) {
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_PACKAGE_INFO + "/" + token, ByteString.copyFrom(new byte[]{}),
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = Connect.StructData.parseFrom(imResponse.getBody());
                            Connect.RedPackage redPackage = Connect.RedPackage.parseFrom(structData.getPlainData());
                            if (ProtoBufUtil.getInstance().checkProtoBuf(redPackage)) {
                                if (redPackage.getRemainSize() == 0) {//lucky packet is brought out
                                    String hashid = redPackage.getHashId();
                                    int type = redPackage.getSystem() ? 1 : 0;
                                    //PacketDetailActivity.startActivity((Activity) context, hashid, type);
                                } else {
                                    UserOrderBean userOrderBean = new UserOrderBean();
                                    userOrderBean.outerRedPacket(token);
                                }
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {

                    }
                });
    }
}