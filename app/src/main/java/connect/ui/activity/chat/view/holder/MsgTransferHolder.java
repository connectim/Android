package connect.ui.activity.chat.view.holder;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import connect.db.green.DaoHelper.TransactionHelper;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.BaseEntity;
import connect.ui.activity.chat.bean.MsgDirect;
import connect.ui.activity.chat.bean.TransferExt;
import connect.ui.activity.chat.exts.TransferDetailActivity;
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
    public void buildRowData(MsgBaseHolder msgBaseHolder, final BaseEntity entity) {
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
                TransferDetailActivity.startActivity((Activity) context, transferExt.getType(), entity.getMsgDefinBean().getContent(),entity.getMsgid());
            }
        });

        if (entity.getTransStatus() == 0) {
            String hashid = entity.getMsgDefinBean().getContent();
            String messageid = entity.getMsgid();
            TransactionHelper.getInstance().updateTransEntity(hashid, messageid, 1);
        }
    }
}
