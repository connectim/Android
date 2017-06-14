package connect.view.album.presenter;

import android.content.Context;

/**
 * Image scanning layer
 * <p/>
 * Created by Clock on 2016/3/19.
 */
public interface ImageScannerPresenter {

    /**
     * Scan image folder list
     *
     * @param context
     * @param selecttype 0:image 1:image and video
     */
    void startScanImage(Context context, int selecttype);

}
