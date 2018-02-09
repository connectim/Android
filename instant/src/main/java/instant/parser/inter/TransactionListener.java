package instant.parser.inter;

import protos.Connect;

/**
 * Created by Administrator on 2017/10/9.
 */

public interface TransactionListener {

    void strangerTransferNotice(Connect.TransferNotice transferNotice);

    void transactionConfirmNotice(Connect.TransactionNotice notice);

    void redpacketGetNotice(Connect.RedPackageNotice notice);

    void singleBillPaymentNotice(Connect.BillNotice billNotice);

    void groupBillPaymentNotice(Connect.CrowdfundingNotice crowdfundingNotice);

    void outerTransfer(Connect.TransferNotice notice);
}
