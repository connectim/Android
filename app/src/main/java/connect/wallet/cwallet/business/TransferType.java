package connect.wallet.cwallet.business;

/**
 * Transfer type
 */

public enum TransferType{
    TransactionTypeBill(1),
    TransactionTypePayCrowding(2),
    TransactionTypeLuckypackage(3),
    TransactionTypeURLTransfer(6);

    private int type;

    TransferType(int type) {
        this.type = type;
    }

    public int getType(){
        return type;
    }

    public static TransferType getType(int type){
        switch (type){
            case 1:
                return TransactionTypeBill;
            case 2:
                return TransactionTypePayCrowding;
            case 3:
                return TransactionTypeLuckypackage;
            case 6:
                return TransactionTypeURLTransfer;
            default:
                break;
        }
        return null;
    }
}
