package connect.widget.album.inter;

import android.content.Context;

import connect.widget.album.model.AlbumFolderType;
import connect.widget.album.presenter.AlbumPresenter;

public interface IAlbumScanner {

    void startScanAlbum(Context context, AlbumFolderType albumFolderType, AlbumPresenter.OnScanListener onScanListener);

}
