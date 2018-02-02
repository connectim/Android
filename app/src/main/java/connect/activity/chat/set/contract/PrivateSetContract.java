package connect.activity.chat.set.contract;

import android.view.View;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/8/7.
 */

public interface PrivateSetContract {

    interface BView extends BaseView<PrivateSetContract.Presenter> {

        String getRoomKey();

        String getAvatar();

        String getName();

        void searchHistoryTxt();

        void switchTop(String name, boolean state);

        void switchDisturb(String name, boolean state);

        void clearMessage();

        void showContactInfo(View view);
    }

    interface Presenter extends BasePresenter {

    }
}
