package connect.activity.wallet.manager;

/**
 * Transfer result type
 */

public enum  TransferResultCode {
    TRANSFER_SUCCESS(0),        // success
    FEETOSAMLL(3000),           // The fee is too small
    FEEEMPTY(3001),             // The fee is empty
    UNSPENTTOOLARGE(3002),      // There are too many transactions
    UNSPENTERROR(3003),
    UNSPENTNOTENOUGH(3004),     // Insufficient balance
    OUTPUTDUST(3005),           // The output amount is too small
    UNSPENTDUST(3006),          // Zero is too small
    AUTOMAX(3007);              // The automatic calculation fee is greater than the maximum threshold

    private int code;

    TransferResultCode(int code) {
        this.code = code;
    }

    public static TransferResultCode getTransferCode(int code){
        switch (code){
            case 0:
                return TRANSFER_SUCCESS;
            case 3000:
                return FEETOSAMLL;
            case 3001:
                return FEEEMPTY;
            case 3002:
                return UNSPENTTOOLARGE;
            case 3003:
                return UNSPENTERROR;
            case 3004:
                return UNSPENTNOTENOUGH;
            case 3005:
                return OUTPUTDUST;
            case 3006:
                return UNSPENTDUST;
            case 3007:
                return AUTOMAX;
            default:
                break;
        }
        return null;
    }

}
