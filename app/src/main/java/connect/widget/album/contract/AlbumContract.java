package connect.widget.album.contract;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import connect.widget.album.model.AlbumFile;
import connect.widget.album.model.AlbumFolder;
import connect.widget.album.model.AlbumFolderType;
import connect.widget.album.presenter.AlbumPresenter;

/**
 * Created by Administrator on 2017/8/21.
 */

public interface AlbumContract {

    interface BView extends BaseView<AlbumContract.Presenter> {

        Activity getActivity();

        Presenter getPresenter();

        AlbumFolderType getAlbumFolderType();

        List<AlbumFolder> getAlbumFolders();

        ArrayList<AlbumFile> getAlbumFiles();

        void setAlbumFiles(ArrayList<AlbumFile> albumFiles);
    }

    interface Presenter extends BasePresenter {

        void albumScan(AlbumPresenter.OnScanListener listener);

        void gridAlbumFragment();

        void galleyFragment(boolean select, int postion);

        void albumFolderDialog();
    }
}
