package connect.activity.chat.view.holder;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import connect.database.green.DaoHelper.TransactionHelper;
import connect.database.green.bean.TransactionEntity;
import connect.ui.activity.R;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.GatherBean;
import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.exts.GatherDetailGroupActivity;
import connect.activity.chat.exts.GatherDetailSingleActivity;
import connect.activity.chat.exts.TransferDetailActivity;
import connect.utils.data.RateFormatUtil;

/**
 * gather
 * Created by gtq on 2016/11/23.
 */
public class MsgPayHolder extends MsgChatHolder {

    private String Tag = "MsgPayHolder";

    private TextView btc;
    private TextView name;
    private TextView pay;

    public MsgPayHolder(View itemView) {
        super(itemView);
        btc = (TextView) itemView.findViewById(R.id.btc);
        name = (TextView) itemView.findViewById(R.id.tvName);
        pay = (TextView) itemView.findViewById(R.id.pay);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final MsgEntity entity) {
        super.buildRowData(msgBaseHolder, entity);
        MsgDefinBean definBean = entity.getMsgDefinBean();

        GatherBean gatherBean = new Gson().fromJson(String.valueOf(definBean.getExt1()), GatherBean.class);
        String amout = RateFormatUtil.longToDoubleBtc(gatherBean.getAmount());

        if (!gatherBean.getIsCrowdfundRceipt()) {
            name.setVisibility(View.VISIBLE);
            String paymentstr = "";
            paymentstr = context.getString(R.string.Wallet_Payment_to_friend);

            btc.setText(String.format(context.getResources().getString(R.string.Chat_Enter_BTC), paymentstr, amout));
            if (entity.getTransStatus() == 0) {
                pay.setText(context.getResources().getString(R.string.Chat_Unpaid));
            } else if (entity.getTransStatus() == 1) {
                pay.setText(context.getResources().getString(R.string.Wallet_Unconfirmed));
            }else if(entity.getTransStatus() == 2){
                pay.setText(context.getResources().getString(R.string.Wallet_Confirmed));
            }
        } else {
            String note = gatherBean.getNote();
            if (TextUtils.isEmpty(note)) {
                name.setVisibility(View.GONE);
            } else {
                name.setVisibility(View.VISIBLE);
                name.setText(note);
            }
            btc.setText(context.getResources().getString(R.string.Chat_Crowd_funding) +
                    context.getResources().getString(R.string.Wallet_crowdfunding_each, amout));
            pay.setText(String.format(context.getResources().getString(R.string.Chat_founded), entity.getPayCount(), gatherBean.getTotalMember()));
        }

        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MsgDefinBean bean = entity.getMsgDefinBean();
                GatherBean gather = new Gson().fromJson(bean.getExt1(), GatherBean.class);
                if (!gather.getIsCrowdfundRceipt()) {
                    if (entity.getTransStatus() == 0) {
                        GatherDetailSingleActivity.startActivity((Activity) context, bean.getContent(), bean.getMessage_id());
                    } else {
                        TransferDetailActivity.startActivity((Activity) context, 0, entity.getMsgDefinBean().getContent(), entity.getMsgid());
                    }
                } else {
                    GatherDetailGroupActivity.startActivity((Activity) context, bean.getContent(), bean.getMessage_id());
                }
            }
        });

//        String hashid = entity.getMsgDefinBean().getContent();
//        TransactionEntity indexEntity = TransactionHelper.getInstance().loadTransEntity(hashid);
//        if (indexEntity == null) {
//            String messageid = entity.getMsgid();
//            if (!gatherBean.getIsCrowdfundRceipt()) {
//                TransactionHelper.getInstance().updateTransEntity(hashid, messageid, 0);
//            } else {
//                TransactionHelper.getInstance().updateTransEntity(hashid, messageid, 0, gatherBean.getTotalMember());
//            }
//        }
    }
}
