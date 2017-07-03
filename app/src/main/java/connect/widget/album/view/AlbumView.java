package connect.widget.album.view;

import connect.widget.album.entity.AlbumFolderInfo;
import connect.widget.album.view.entity.AlbumViewData;

/**
 * Created by Clock on 2016/3/19.
 */
public interface AlbumView {

    /**
     * Refresh the album data
     *
     * @param albumData
     */
    void refreshAlbumData(AlbumViewData albumData);

    /**
     * Switch the images directory
     *
     * @param albumFolderInfo
     */
    void switchAlbumFolder(AlbumFolderInfo albumFolderInfo);

}
