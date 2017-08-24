package connect.widget.album.inter;

import android.content.Context;

import connect.widget.album.model.AlbumType;
import connect.widget.album.presenter.AlbumPresenter;

public interface IAlbumScanner {

    void startScanAlbum(Context context, AlbumType albumType, AlbumPresenter.OnScanListener onScanListener);

}
