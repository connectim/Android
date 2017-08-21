package connect.widget.album.contract;

import android.app.Activity;

import java.util.List;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import connect.widget.album.entity.AlbumFolderInfo;

/**
 * Created by Administrator on 2017/8/21.
 */

public interface AlbumContract {

    interface BView extends BaseView<AlbumContract.Presenter> {

        Activity getActivity();

    }

    interface Presenter extends BasePresenter {
        List<AlbumFolderInfo> albumScan();
    }

}
