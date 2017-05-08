package connect.view.album.view.entity;

import connect.view.album.entity.AlbumFolderInfo;

import java.util.List;

/**
 * Album interface data
 * <p/>
 * Created by Clock on 2016/3/21.
 */
public class AlbumViewData {

    /**
     * Album list
     */
    private List<AlbumFolderInfo> albumFolderInfoList;

    public List<AlbumFolderInfo> getAlbumFolderInfoList() {
        return albumFolderInfoList;
    }

    public void setAlbumFolderInfoList(List<AlbumFolderInfo> albumFolderInfoList) {
        this.albumFolderInfoList = albumFolderInfoList;
    }
}
