package connect.activity.contact.contract;

import android.app.Activity;

import connect.database.green.bean.ContactEntity;
import connect.activity.home.bean.MsgNoticeBean;
import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/4/19 0019.
 */

public interface FriendInfoContract {

    interface View extends BaseView<FriendInfoContract.Presenter> {
        Activity getActivity();

        void updataView(ContactEntity friendEntity);

        void setCommon(boolean isCommon);

        void setBlock(boolean block);
    }

    interface Presenter extends BasePresenter {
        void requestUserInfo(String address, ContactEntity friendEntity);

        void checkOnEvent(MsgNoticeBean notice);

        void requestBlock(boolean block,String address);
    }

}
