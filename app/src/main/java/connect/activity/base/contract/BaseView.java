package connect.activity.base.contract;

import android.app.Activity;

/**
 * base View
 */
public interface BaseView<T> {

    void setPresenter(T presenter);

    Activity getActivity();
}
