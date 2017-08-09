package connect.activity.chat.set.contract;

import android.view.View;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/8/8.
 */

public interface GroupSetContract {

    interface BView extends BaseView<GroupSetContract.Presenter> {

        String getRoomKey();

        void countMember(String members);

        void memberList(View view);

        void groupNameClickable(boolean clickable);

        void groupName(String groupname);

        void groupMyAlias(String alias);

        void groupQRCode();

        void groupManager(boolean visiable);

        void topSwitch(boolean top);

        void noticeSwitch(boolean notice);

        void commonSwtich(boolean common);

        void clearHistory();

        void exitGroup();
    }

    interface Presenter extends BasePresenter {

        void syncGroupInfo();

        void updateGroupMute(boolean state);

        void updateGroupCommon(boolean state);

        void requestExitGroup();
    }
}
