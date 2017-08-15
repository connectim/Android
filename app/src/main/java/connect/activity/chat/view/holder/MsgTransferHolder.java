package connect.activity.chat.view.holder;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.exts.TransferMutiDetailActivity;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.ui.activity.R;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.bean.TransferExt;
import connect.activity.chat.exts.TransferSingleDetailActivity;
import connect.utils.data.RateFormatUtil;

/**
 * transfer
 * Created by gtq on 2016/11/23.
 */
public class MsgTransferHolder extends MsgChatHolder {

    private TextView transferTxt;
    private TextView stateTxt;

    private TransferExt transferExt = null;

    public MsgTransferHolder(View itemView) {
        super(itemView);
        transferTxt = (TextView) itemView.findViewById(R.id.txt1);
        stateTxt = (TextView) itemView.findViewById(R.id.txt2);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final MsgExtEntity msgExtEntity) {
        super.buildRowData(msgBaseHolder, entity);
        if (entity.getTransStatus() <= 1) {
            stateTxt.setText(context.getString(R.string.Wallet_Unconfirmed));
        } else if (entity.getTransStatus() == 2) {
            stateTxt.setText(context.getString(R.string.Wallet_Confirmed));
        }

        transferExt = new Gson().fromJson(String.valueOf(entity.getMsgDefinBean().getExt1()), TransferExt.class);
        String transAmount = RateFormatUtil.longToDoubleBtc(transferExt.getAmount());
        String transInfo = direct == MsgDirect.From ? context.getString(R.string.Chat_Transfer_to_you_BTC, transAmount) :
                context.getString(R.string.Chat_Transfer_to_other_BTC, transAmount);
        transferTxt.setText(transInfo);
        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MsgDefinBean definBean = entity.getMsgDefinBean();
                String sender = definBean.getSenderInfoExt().getPublickey();
                String receiver = definBean.getPublicKey();
                String hashid = definBean.getContent();
                String msgid = definBean.getMessage_id();

                int transferType = transferExt.getType();
                if (transferType == 0 || transferType == 1) {
                    TransferSingleDetailActivity.startActivity((Activity) context, transferType, sender, receiver, hashid, msgid);
                } else if (transferType == 2) {
                    TransferMutiDetailActivity.startActivity((Activity) context, hashid, msgid);
                }
            }
        });

        if (entity.getTransStatus() == 0) {
            String hashid = entity.getMsgDefinBean().getContent();
            String messageid = entity.getMsgid();
            TransactionHelper.getInstance().updateTransEntity(hashid, messageid, 1);
        }
    }
}
