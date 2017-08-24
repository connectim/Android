package connect.widget.album.contract;

import android.app.Activity;

import java.util.List;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import connect.widget.album.model.AlbumFolderInfo;
import connect.widget.album.model.AlbumType;
import connect.widget.album.model.ImageInfo;
import connect.widget.album.presenter.AlbumPresenter;

/**
 * Created by Administrator on 2017/8/21.
 */

public interface AlbumContract {

    interface BView extends BaseView<AlbumContract.Presenter> {

        Activity getActivity();

        Presenter getPresenter();

        AlbumType getAlbumType();

        List<AlbumFolderInfo> getFolderInfos();

        List<ImageInfo> getImageInfos();

        void setImageInfos(List<ImageInfo> imageInfos);
    }

    interface Presenter extends BasePresenter {

        void albumScan(AlbumPresenter.OnScanListener listener);

        void gridAlbumFragment();

        void galleyFragment(boolean select, int postion);

        void albumFolderDialog();
    }
}
