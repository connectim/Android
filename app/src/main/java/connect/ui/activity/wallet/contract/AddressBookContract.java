package connect.ui.activity.wallet.contract;

import android.app.Activity;

import java.util.ArrayList;

import connect.ui.activity.wallet.bean.AddressBean;
import connect.ui.base.BasePresenter;
import connect.ui.base.BaseView;

/**
 * Created by Administrator on 2017/4/18 0018.
 */

public interface AddressBookContract {

    interface View extends BaseView<AddressBookContract.Presenter> {
        Activity getActivity();

        void updataView(ArrayList<AddressBean> listAddress);
    }

    interface Presenter extends BasePresenter {
        void requestAddressBook();

        void requestAddAddress(String address);

        void requestSetTag(String address, String tag, int position);

        void requestRemove(String address, int position);
    }

}
