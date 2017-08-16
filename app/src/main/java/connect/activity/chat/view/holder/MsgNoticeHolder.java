package connect.activity.chat.view.holder;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.protobuf.InvalidProtocolBufferException;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.contact.StrangerInfoActivity;
import connect.activity.contact.bean.SourceType;
import connect.activity.wallet.PacketDetailActivity;
import connect.database.MemoryDataManager;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import protos.Connect;

/**
 * Created by gtq on 2016/12/19.
 */
public class MsgNoticeHolder extends MsgBaseHolder {
    private LinearLayout noticeLayout;
    private TextView notice;

    public MsgNoticeHolder(View itemView) {
        super(itemView);
        noticeLayout = (LinearLayout) itemView.findViewById(R.id.content_notice);
        notice = (TextView) itemView.findViewById(R.id.notify);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final MsgExtEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        SpannableStringBuilder builder = null;
        SpannableStringBuilder colorBuilder = null;
        SpannableStringBuilder stringBuilder = null;
        ForegroundColorSpan colorSpan = null;

        Drawable drawable = null;
        ImageSpan imageSpan = null;

        MsgType msgType=MsgType.toMsgType(msgExtEntity.getMessageType());
        switch (msgType) {
            case NOTICE_STRANGER://stranger
                builder = new SpannableStringBuilder();
                stringBuilder = new SpannableStringBuilder(context.getString(R.string.Chat_Add_as_a_friend_to_chat));
                builder.append(stringBuilder);
                colorBuilder = new SpannableStringBuilder(context.getString(R.string.Link_Add_as_a_friend));
                colorSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.color_blue));
                colorBuilder.setSpan(colorSpan, 0, colorBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.append(colorBuilder);
                notice.setText(builder);
                noticeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Connect.NotifyMessage notifyMessage = Connect.NotifyMessage.parseFrom(msgExtEntity.getContents());
                            StrangerInfoActivity.startActivity((Activity) context, notifyMessage.getContent(), SourceType.SEARCH);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case NOTICE_BLACK://black friend
                notice.setText(context.getString(R.string.Link_Add_as_a_friend));
                break;
            case NOTICE_NOTMEMBER://not group member
                notice.setText(context.getString(R.string.Message_send_fail_not_in_group));
                break;
            case OUTERPACKET_GET://External envelope was received
                final Connect.SystemRedpackgeNotice redpackgeNotice = Connect.SystemRedpackgeNotice.parseFrom(msgExtEntity.getContents());

                String mypubkey = MemoryDataManager.getInstance().getPubKey();
                Connect.UserInfo userInfo = redpackgeNotice.getReceiver();
                String receiverName = userInfo.getPubKey().equals(mypubkey) ?
                        context.getString(R.string.Chat_You) : userInfo.getUsername();

                builder = new SpannableStringBuilder(" " + context.getString(R.string.Chat_opened_Lucky_Packet_of, receiverName, context.getString(R.string.Chat_You)));
                drawable = context.getResources().getDrawable(R.mipmap.luckybag3x);
                drawable.setBounds(0, 0, 20, 20);
                imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
                builder.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                colorBuilder = new SpannableStringBuilder(context.getString(R.string.Wallet_Detail));
                colorSpan = new ForegroundColorSpan(Color.BLUE);
                colorBuilder.setSpan(colorSpan, 0, colorBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.append(colorBuilder);

                notice.setText(builder);
                noticeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PacketDetailActivity.startActivity((Activity) context, redpackgeNotice.getHashid());
                    }
                });
                break;
            case NOTICE_CLICKRECEIVEPACKET://click to get lucky packet
                Connect.NotifyMessage notifyMessage= Connect.NotifyMessage.parseFrom(msgExtEntity.getContents());
                builder = new SpannableStringBuilder("  " + notifyMessage.getContent());
                drawable = context.getResources().getDrawable(R.mipmap.luckybag3x);
                drawable.setBounds(0, 0, 20, 20);
                imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
                builder.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                notice.setText(builder);
                break;
            default:
                String showTxt = context.getString(R.string.Chat_Msg_Undefine);
                Connect.NotifyMessage defaultNotify= Connect.NotifyMessage.parseFrom(msgExtEntity.getContents());
                if (!TextUtils.isEmpty(defaultNotify.getContent())) {
                    showTxt = defaultNotify.getContent();
                }
                notice.setText(showTxt);
                break;
        }
    }
}
