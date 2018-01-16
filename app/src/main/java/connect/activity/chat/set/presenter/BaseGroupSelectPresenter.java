package connect.activity.chat.set.presenter;

import android.app.Activity;

import connect.activity.chat.set.contract.BaseGroupSelectContract;

/**
 * Created by PuJin on 2018/1/11.
 */

public class BaseGroupSelectPresenter implements BaseGroupSelectContract.Presenter {

    private BaseGroupSelectContract.BView view;
    private String pubKey;
    private Activity activity;

    public BaseGroupSelectPresenter(BaseGroupSelectContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        pubKey = view.getRoomKey();
        activity = view.getActivity();
    }
}
