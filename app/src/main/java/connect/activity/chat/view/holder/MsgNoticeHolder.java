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

import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.MsgSender;
import connect.activity.contact.StrangerInfoActivity;
import connect.activity.contact.bean.SourceType;
import connect.activity.wallet.PacketDetailActivity;
import connect.database.MemoryDataManager;
import connect.ui.activity.R;
import connect.utils.TimeUtil;

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
    public void buildRowData(MsgBaseHolder msgBaseHolder, final MsgEntity entity) {
        super.buildRowData(msgBaseHolder, entity);
        SpannableStringBuilder builder = null;
        SpannableStringBuilder colorBuilder = null;
        SpannableStringBuilder stringBuilder = null;
        ForegroundColorSpan colorSpan = null;

        Drawable drawable = null;
        ImageSpan imageSpan = null;

        MsgDefinBean definBean = entity.getMsgDefinBean();
        switch (definBean.getType()) {
            case -9://stranger
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
                        StrangerInfoActivity.startActivity((Activity) context, ((MsgEntity) entity).getRecAddress(), SourceType.SEARCH);
                    }
                });
                break;
            case -10://black friend
                notice.setText(context.getString(R.string.Link_Add_as_a_friend));
                break;
            case -11://not group member
                notice.setText(context.getString(R.string.Message_send_fail_not_in_group));
                break;
            case 11://open/close burn
                String name = "";
                String content = "";
                String myPubkey=MemoryDataManager.getInstance().getPubKey();
                if (entity.getMsgDefinBean().getPublicKey().equals(myPubkey)) {
                    MsgSender msgSender = entity.getMsgDefinBean().getSenderInfoExt();
                    name = msgSender.username;
                } else {
                    name = context.getResources().getString(R.string.Chat_You);
                }

                if (TextUtils.isEmpty(definBean.getContent()) || "0".equals(definBean.getContent())) {
                    content = context.getResources().getString(R.string.Chat_disable_the_self_descruct, name);
                } else {
                    content = context.getResources().getString(R.string.Chat_set_the_self_destruct_timer_to, name, TimeUtil.parseBurnTime(definBean.getContent()));
                }
                notice.setText(content);
                break;
            case 103://External envelope was received
                MsgSender msgSender = entity.getMsgDefinBean().getSenderInfoExt();
                String receiverName = MemoryDataManager.getInstance().getPubKey().equals(msgSender.publickey) ?
                        context.getString(R.string.Chat_You) : msgSender.username;

                builder = new SpannableStringBuilder(" "+context.getString(R.string.Chat_opened_Lucky_Packet_of, receiverName,context.getString(R.string.Chat_You)));
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
                        PacketDetailActivity.startActivity((Activity) context,entity.getMsgDefinBean().getContent());
                    }
                });
                break;
            case -501://click to get lucky packet
                builder = new SpannableStringBuilder("  " + definBean.getContent());
                drawable = context.getResources().getDrawable(R.mipmap.luckybag3x);
                drawable.setBounds(0, 0, 20, 20);
                imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
                builder.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                notice.setText(builder);
                break;
            default:
                String showTxt = context.getString(R.string.Chat_Msg_Undefine);
                if (definBean != null && !TextUtils.isEmpty(definBean.getContent())) {
                    showTxt = definBean.getContent();
                }
                notice.setText(showTxt);
                break;
        }
    }
}
