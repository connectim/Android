package connect.view.album.presenter.entity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The results of scanning images
 * <p/>
 * Created by Clock on 2016/3/21.
 */
public class ImageScanResult {

    /**
     * System all have pictures folder
     */
    private List<File> albumFolderList;
    /**
     * Each folder contains below pictures
     */
    private Map<String, ArrayList<File>> albumImageListMap;

    /**
     * For all have pictures on your mobile phone directory
     *
     * @return
     */
    public List<File> getAlbumFolderList() {
        return albumFolderList;
    }

    public void setAlbumFolderList(List<File> albumFolderList) {
        this.albumFolderList = albumFolderList;
    }

    /**
     * Get all pictures on your mobile phone directory contains pictures
     *
     * @return A Map, key is the picture directory path, value is the corresponding directory contains all the image files
     */
    public Map<String, ArrayList<File>> getAlbumImageListMap() {
        return albumImageListMap;
    }

    public void setAlbumImageListMap(Map<String, ArrayList<File>> albumImageListMap) {
        this.albumImageListMap = albumImageListMap;
    }

}
