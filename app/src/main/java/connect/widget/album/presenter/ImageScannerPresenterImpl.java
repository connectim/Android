package connect.widget.album.presenter;

import android.content.Context;

import java.util.List;

import connect.widget.album.entity.AlbumFolderInfo;
import connect.widget.album.model.ImageScannerModel;
import connect.widget.album.model.ImageScannerModelImpl;
import connect.widget.album.view.AlbumView;
import connect.widget.album.view.entity.AlbumViewData;

/**
 * Image scanning Presenter implementation class
 * <p/>
 * Created by Clock on 2016/3/21.
 */
public class ImageScannerPresenterImpl implements ImageScannerPresenter {

    private ImageScannerModel mScannerModel;
    private AlbumView mAlbumView;

    public ImageScannerPresenterImpl(AlbumView albumView) {
        mScannerModel = new ImageScannerModelImpl();
        mAlbumView = albumView;
    }

    @Override
    public void startScanImage(final Context context, int selecttype) {
        mScannerModel.startScanAlbum(context, selecttype, new ImageScannerModel.OnScanImageFinish() {
            @Override
            public void onFinish(List<AlbumFolderInfo> infoList) {
                if (mAlbumView != null) {
                    AlbumViewData albumData = new AlbumViewData();
                    albumData.setAlbumFolderInfoList(infoList);
                    mAlbumView.refreshAlbumData(albumData);
                }
            }
        });
    }
}
