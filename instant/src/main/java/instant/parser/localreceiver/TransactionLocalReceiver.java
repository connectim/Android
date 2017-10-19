package instant.parser.localreceiver;

import instant.parser.inter.TransactionListener;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/9.
 */

public class TransactionLocalReceiver implements TransactionListener {

    public static TransactionLocalReceiver localReceiver = getInstance();

    private synchronized static TransactionLocalReceiver getInstance() {
        if (localReceiver == null) {
            localReceiver = new TransactionLocalReceiver();
        }
        return localReceiver;
    }

    private TransactionListener transactionListener;

    public void registerTransactionListener(TransactionListener listener){
        this.transactionListener=listener;
    }

    public TransactionListener getTransactionListener() {
        if (transactionListener == null) {
            throw new RuntimeException("transactionListener don't registe");
        }
        return transactionListener;
    }

    @Override
    public void strangerTransferNotice(Connect.TransferNotice transferNotice) {
        getTransactionListener().strangerTransferNotice(transferNotice);
    }

    @Override
    public void transactionConfirmNotice(Connect.TransactionNotice notice) {
        getTransactionListener().transactionConfirmNotice(notice);
    }

    @Override
    public void redpacketGetNotice(Connect.RedPackageNotice notice) {
        getTransactionListener().redpacketGetNotice(notice);
    }

    @Override
    public void singleBillPaymentNotice(Connect.BillNotice billNotice) {
        getTransactionListener().singleBillPaymentNotice(billNotice);
    }

    @Override
    public void groupBillPaymentNotice(Connect.CrowdfundingNotice crowdfundingNotice) {
        getTransactionListener().groupBillPaymentNotice(crowdfundingNotice);
    }

    @Override
    public void outerTransfer(Connect.TransferNotice notice) {
        getTransactionListener().outerTransfer(notice);
    }
}
