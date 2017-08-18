package connect.activity.chat.view.holder;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.exts.TransferMutiDetailActivity;
import connect.activity.chat.exts.TransferSingleDetailActivity;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.ui.activity.R;
import connect.utils.data.RateFormatUtil;
import protos.Connect;

/**
 * transfer
 * Created by gtq on 2016/11/23.
 */
public class MsgTransferHolder extends MsgChatHolder {

    private TextView transferTxt;
    private TextView stateTxt;

    public MsgTransferHolder(View itemView) {
        super(itemView);
        transferTxt = (TextView) itemView.findViewById(R.id.txt1);
        stateTxt = (TextView) itemView.findViewById(R.id.txt2);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final MsgExtEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        final Connect.TransferMessage transferMessage = Connect.TransferMessage.parseFrom(msgExtEntity.getContents());

        stateTxt.setText(msgExtEntity.getTransStatus() <= 1 ? context.getString(R.string.Wallet_Unconfirmed) :
                context.getString(R.string.Wallet_Confirmed));

        String transAmount = RateFormatUtil.longToDoubleBtc(transferMessage.getAmount());
        String transInfo = msgExtEntity.parseDirect() == MsgDirect.From ? context.getString(R.string.Chat_Transfer_to_you_BTC, transAmount) :
                context.getString(R.string.Chat_Transfer_to_other_BTC, transAmount);
        transferTxt.setText(transInfo);

        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sender = msgExtEntity.getMessage_from();
                String receiver = msgExtEntity.getMessage_to();
                String hashid = transferMessage.getHashId();
                String msgid = msgExtEntity.getMessage_id();

                int transferType = transferMessage.getTransferType();
                if (transferType == 0 || transferType == 1) {
                    TransferSingleDetailActivity.startActivity((Activity) context, transferType, sender, receiver, hashid, msgid);
                } else if (transferType == 2) {
                    TransferMutiDetailActivity.startActivity((Activity) context, hashid, msgid);
                }
            }
        });

        if (msgExtEntity.getTransStatus() == 0) {
            String hashid = msgExtEntity.getHashid();
            String messageid = msgExtEntity.getMessage_id();
            TransactionHelper.getInstance().updateTransEntity(hashid, messageid, 1);
        }
    }
}
