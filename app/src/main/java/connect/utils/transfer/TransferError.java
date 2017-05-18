package connect.utils.transfer;

import android.widget.Toast;

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

    private void showError(int code, String massage){

        if(code == 2666){
            //Toast.makeText(BaseApplication.getInstance(),)
        }

    }

}
