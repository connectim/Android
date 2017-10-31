package connect.activity.chat.view.holder;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import instant.bean.ChatMsgEntity;
import instant.bean.MsgDirect;
import connect.activity.chat.exts.CrowdingDetailActivity;
import connect.activity.chat.exts.PaymentDetailActivity;
import connect.activity.chat.exts.TransferSingleDetailActivity;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.database.green.bean.TransactionEntity;
import connect.ui.activity.R;
import connect.utils.data.RateFormatUtil;
import protos.Connect;

/**
 * gather
 * Created by gtq on 2016/11/23.
 */
public class MsgPayHolder extends MsgChatHolder {

    private static String TAG = "_MsgPayHolder";

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
    public void buildRowData(MsgBaseHolder msgBaseHolder, final ChatMsgEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        final Connect.PaymentMessage paymentMessage = Connect.PaymentMessage.parseFrom(msgExtEntity.getContents());

        String amout = RateFormatUtil.longToDoubleBtc(paymentMessage.getAmount());
        if (paymentMessage.getPaymentType() == 0) {
            name.setVisibility(View.VISIBLE);
            String paymentstr = msgExtEntity.parseDirect() == MsgDirect.From
                    ? context.getString(R.string.Wallet_Receipt)
                    : context.getString(R.string.Wallet_Payment_to_friend);

            btc.setText(String.format(context.getResources().getString(R.string.Chat_Enter_BTC), paymentstr, amout));
            if (msgExtEntity.getTransStatus() == 0) {
                pay.setText(context.getResources().getString(R.string.Chat_Unpaid));
            } else if (msgExtEntity.getTransStatus() == 1) {
                pay.setText(context.getResources().getString(R.string.Wallet_Unconfirmed));
            }else if(msgExtEntity.getTransStatus() == 2){
                pay.setText(context.getResources().getString(R.string.Wallet_Confirmed));
            }
        } else {
            String note = paymentMessage.getTips();
            if (TextUtils.isEmpty(note)) {
                name.setVisibility(View.GONE);
            } else {
                name.setVisibility(View.VISIBLE);
                name.setText(note);
            }
            btc.setText(context.getResources().getString(R.string.Chat_Crowd_funding) +
                    context.getResources().getString(R.string.Wallet_crowdfunding_each, amout));
            pay.setText(String.format(context.getResources().getString(R.string.Chat_founded), msgExtEntity.getPayCount(), msgExtEntity.getCrowdCount()));
        }

        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paymentMessage.getPaymentType() == 0) {
                    if (msgExtEntity.getTransStatus() == 0) {
                        PaymentDetailActivity.startActivity((Activity) context, msgExtEntity);
                    } else {
                        int transferType = 0;
                        String hashid = paymentMessage.getHashId();
                        String msgid = msgExtEntity.getMessage_id();
                        TransferSingleDetailActivity.startActivity((Activity) context, transferType, hashid, msgid);
                    }
                } else {
                    CrowdingDetailActivity.startActivity((Activity) context, paymentMessage.getHashId(), msgExtEntity.getMessage_id());
                }
            }
        });

        String hashid = paymentMessage.getHashId();
        TransactionEntity indexEntity = TransactionHelper.getInstance().loadTransEntity(hashid);
        if (indexEntity == null) {
            String messageid = msgExtEntity.getMessage_id();
            if (paymentMessage.getPaymentType() == 0) {
                TransactionHelper.getInstance().updateTransEntity(hashid, messageid, 0);
            } else {
                TransactionHelper.getInstance().updateTransEntity(hashid, messageid, 0, paymentMessage.getMemberSize());
            }
        }
    }
}
