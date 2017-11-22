package connect.activity.chat.subscribe.presenter;

import connect.activity.chat.subscribe.contract.SubscribeListContract;

/**
 * Created by Administrator on 2017/11/22.
 */

public class SubscribeListPresenter implements SubscribeListContract.Presenter {

    private SubscribeListContract.BView bView;

    public SubscribeListPresenter(SubscribeListContract.BView bView) {
        this.bView = bView;
    }

    @Override
    public void start() {

    }
}
