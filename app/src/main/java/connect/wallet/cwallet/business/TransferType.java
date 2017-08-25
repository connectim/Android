package connect.wallet.cwallet.business;

/**
 * Transfer type
 */

public enum TransferType{
    TransactionTypeBill(1),
    TransactionTypePayment(2),
    TransactionTypeLuckyPackage(3),
    TransactionTypeURLTransfer(6),
    TransactionTypePayCrowding(8);

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
                return TransactionTypePayment;
            case 3:
                return TransactionTypeLuckyPackage;
            case 6:
                return TransactionTypeURLTransfer;
            case 8:
                return TransactionTypePayCrowding;
            default:
                break;
        }
        return null;
    }
}
