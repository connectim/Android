package connect.view.album.view;

import connect.view.album.entity.ImageInfo;

/**
 *Picture selector View layer interface
 * <p/>
 * Created by Clock on 2016/3/21.
 */
public interface ImageChooseView {

    /**
     * Refresh image counter
     *
     * @param imageInfo File information for operation
     */
    void refreshSelectedCounter(ImageInfo imageInfo);

    int selectCount();

}
