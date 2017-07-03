package connect.ui.activity.contact.contract;

import android.app.Activity;

import connect.db.green.bean.ContactEntity;
import connect.ui.activity.home.bean.MsgNoticeBean;
import connect.ui.base.BasePresenter;
import connect.ui.base.BaseView;

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
