package connect.utils.transfer;

import android.widget.Toast;

import connect.ui.activity.R;
import connect.ui.base.BaseApplication;

/**
 * Created by Administrator on 2017/5/18 0018.
 */

public class TransferError {

    private static TransferError transferError;

    public static TransferError getInstance() {
        if (null == transferError) {
            transferError = new TransferError();
        }
        return transferError;
    }

    public void showError(int code, String massage){
        switch (code){
            case 2015:
            case 2400:
                Toast.makeText(BaseApplication.getInstance(), R.string.ErrorCode_xn_mempool_conflict,Toast.LENGTH_LONG).show();
                Toast.makeText(BaseApplication.getInstance(), R.string.ErrorCode_xn_mempool_conflict,Toast.LENGTH_LONG).show();
                break;
            case 2616:
                Toast.makeText(BaseApplication.getInstance(), R.string.ErrorCode_Transaction_information_is_incorrect,Toast.LENGTH_LONG).show();
                break;
            case 2617:
                Toast.makeText(BaseApplication.getInstance(), R.string.ErrorCode_transaction_address_is_not_available,Toast.LENGTH_LONG).show();
                break;
            case 2618:
                Toast.makeText(BaseApplication.getInstance(), R.string.ErrorCode_Version_information_is_repeated,Toast.LENGTH_LONG).show();
                break;
            case 2665:
            case 2664:
                Toast.makeText(BaseApplication.getInstance(), R.string.ErrorCode_dust,Toast.LENGTH_LONG).show();
                break;
            case 2666:
                Toast.makeText(BaseApplication.getInstance(), R.string.ErrorCode_The_fee_is_too_low,Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(BaseApplication.getInstance(), code + "--" + massage,Toast.LENGTH_LONG).show();
                break;
        }
    }

}
