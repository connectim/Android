package connect.activity.chat.subscribe.presenter;

import connect.activity.chat.subscribe.contract.SubscribeMessageContract;

/**
 * Created by Administrator on 2017/11/22.
 */

public class SubscribeMessagePresenter implements SubscribeMessageContract.Presenter {

    private SubscribeMessageContract.BView bView;

    public SubscribeMessagePresenter(SubscribeMessageContract.BView bView) {
        this.bView = bView;
    }

    @Override
    public void start() {

    }
}
