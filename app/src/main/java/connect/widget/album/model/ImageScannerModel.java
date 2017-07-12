package connect.widget.album.model;

import android.content.Context;

import java.util.List;

import connect.widget.album.entity.AlbumFolderInfo;

/**
 * Image scanning Model layer interface
 * <p/>
 * Created by Clock on 2016/3/19.
 */
public interface ImageScannerModel {

    /**
     * Access to all images of the list of information (picture directory absolute path as the map key, value is the picture directory of all the image file information)
     *
     * @param context
     * @param selecttype
     * @param onScanImageFinish Scan image to end the callback interface
     * @return
     */
    void startScanAlbum(Context context, int selecttype, OnScanImageFinish onScanImageFinish);

    /**
     * Image scan results callback interface
     */
    interface OnScanImageFinish {

        /**
         * This function is executed at the end of the scan
         */
        void onFinish(List<AlbumFolderInfo> infoList);
    }
}
