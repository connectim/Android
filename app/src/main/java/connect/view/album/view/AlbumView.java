package connect.view.album.view;

import connect.view.album.entity.AlbumFolderInfo;
import connect.view.album.view.entity.AlbumViewData;

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
