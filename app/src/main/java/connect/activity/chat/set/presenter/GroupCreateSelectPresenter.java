package connect.activity.chat.set.presenter;

import android.app.Activity;

import java.util.ArrayList;

import connect.activity.chat.set.GroupCreateActivity;
import connect.activity.chat.set.contract.GroupCreateSelectContract;
import connect.database.green.bean.ContactEntity;

/**
 * Created by Administrator on 2017/8/9.
 */

public class GroupCreateSelectPresenter implements GroupCreateSelectContract.Presenter{

    private GroupCreateSelectContract.BView view;
    private Activity activity;


    public GroupCreateSelectPresenter(GroupCreateSelectContract.BView view){
        this.view=view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        activity = view.getActivity();
    }

    @Override
    public void requestGroupCreate(ArrayList<ContactEntity> entities) {
        GroupCreateActivity.startActivity(activity,entities);
    }
}
