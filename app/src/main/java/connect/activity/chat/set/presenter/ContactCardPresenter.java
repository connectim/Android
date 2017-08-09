package connect.activity.chat.set.presenter;

import android.app.Activity;

import connect.activity.chat.set.contract.ContactCardContract;

/**
 * Created by Administrator on 2017/8/9.
 */

public class ContactCardPresenter implements ContactCardContract.Presenter{

    private ContactCardContract.BView view;
    private String pubKey;
    private Activity activity;

    public ContactCardPresenter(ContactCardContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        pubKey = view.getRoomKey();
        activity = view.getActivity();
    }
}
