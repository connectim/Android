package instant.parser;

import com.google.protobuf.ByteString;

import instant.parser.localreceiver.TransactionLocalReceiver;
import protos.Connect;

/**
 * transaction notice
 * Created by pujin on 2017/4/19.
 */

public class TransactionParser extends InterParse {
    private Connect.NoticeMessage noticeMessage;

    public TransactionParser(Connect.NoticeMessage noticeMessage) {
        super((byte) 5, null);
        this.noticeMessage = noticeMessage;
    }

    /**
     * Transaction notice
     *
     * @throws Exception
     */
    @Override
    public synchronized void msgParse() throws Exception {
        int category = noticeMessage.getCategory();
        ByteString byteString = noticeMessage.getBody();
        switch (category) {
            case 0://The stranger transfer
                Connect.TransferNotice transferNotice = Connect.TransferNotice.parseFrom(byteString.toByteArray());
                strangerTransferNotice(transferNotice);
                break;
            case 1://Transfer transaction confirmation notice
                Connect.TransactionNotice transactionNotice = Connect.TransactionNotice.parseFrom(byteString.toByteArray());
                transactionConfirmNotice(transactionNotice);
                break;
            case 2://To receive a red packet to inform
                Connect.RedPackageNotice redPackgeNotice = Connect.RedPackageNotice.parseFrom(byteString.toByteArray());
                redpacketGetNotice(redPackgeNotice);
                break;
            case 3://The bill payment notice
            case 5://The bill payment notice
                Connect.BillNotice billNotice = Connect.BillNotice.parseFrom(byteString.toByteArray());
                singleBillPaymentNotice(billNotice);
                break;
            case 4://outer transfer
                Connect.TransferNotice notice = Connect.TransferNotice.parseFrom(byteString.toByteArray());
                outerTransfer(notice);
                break;
            case 6://The payment pay notice
                Connect.CrowdfundingNotice crowdfundingNotice = Connect.CrowdfundingNotice.parseFrom(byteString.toByteArray());
                groupBillPaymentNotice(crowdfundingNotice);
                break;
        }
    }

    private void strangerTransferNotice(Connect.TransferNotice notice) {
        TransactionLocalReceiver.localReceiver.strangerTransferNotice(notice);
   }

    /**
     * Transfer transaction confirmation
     *
     * @param notice
     */
    private void transactionConfirmNotice(Connect.TransactionNotice notice) {
        TransactionLocalReceiver.localReceiver.transactionConfirmNotice(notice);
    }

    /**
     * red packet is received notice
     *
     * @param notice
     */
    private void redpacketGetNotice(Connect.RedPackageNotice notice) {
        TransactionLocalReceiver.localReceiver.redpacketGetNotice(notice);
    }

    private void singleBillPaymentNotice(Connect.BillNotice billNotice) {
        TransactionLocalReceiver.localReceiver.singleBillPaymentNotice(billNotice);
    }

    private void groupBillPaymentNotice(Connect.CrowdfundingNotice crowdfundingNotice) {
        TransactionLocalReceiver.localReceiver.groupBillPaymentNotice(crowdfundingNotice);
    }

    /**
     * outer transaction
     */
    private void outerTransfer(Connect.TransferNotice notice) {
        TransactionLocalReceiver.localReceiver.outerTransfer(notice);
    }
}
