package connect.instant.receiver;

import instant.parser.inter.TransactionListener;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/10.
 */

public class TransactionReceiver implements TransactionListener {

    private String Tag = "_TransactionReceiver";

    public static TransactionReceiver receiver = getInstance();

    private synchronized static TransactionReceiver getInstance() {
        if (receiver == null) {
            receiver = new TransactionReceiver();
        }
        return receiver;
    }

    @Override
    public void strangerTransferNotice(Connect.TransferNotice transferNotice) {

    }

    @Override
    public void transactionConfirmNotice(Connect.TransactionNotice notice) {

    }

    @Override
    public void redpacketGetNotice(Connect.RedPackageNotice notice) {

    }

    @Override
    public void singleBillPaymentNotice(Connect.BillNotice billNotice) {

    }

    @Override
    public void groupBillPaymentNotice(Connect.CrowdfundingNotice crowdfundingNotice) {

    }

    @Override
    public void outerTransfer(Connect.TransferNotice notice) {

    }
}
