package connect.view.album.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * catalog information
 */
public class AlbumFolderInfo implements Serializable {

    /** Directory name */
    private String folderName;
    /** Contains all the image information */
    private ArrayList<ImageInfo> imageInfoList;
    /** First picture */
    private ExFile frontCover;
    /** Folder type 0: image 1: video */
    private int albumType;

    public AlbumFolderInfo() {
    }

    public AlbumFolderInfo(ExFile frontCover, String folderName) {
        this.frontCover = frontCover;
        this.folderName = folderName;
    }

    public ExFile getFrontCover() {
        return frontCover;
    }

    public void setFrontCover(ExFile frontCover) {
        this.frontCover = frontCover;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public ArrayList<ImageInfo> getImageInfoList() {
        return imageInfoList;
    }

    public void setImageInfoList(ArrayList<ImageInfo> imageInfoList) {
        this.imageInfoList = imageInfoList;
    }

    public int getAlbumType() {
        return albumType;
    }

    public void setAlbumType(int albumType) {
        this.albumType = albumType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AlbumFolderInfo that = (AlbumFolderInfo) o;

        if (getFolderName() != null ? !getFolderName().equals(that.getFolderName()) : that.getFolderName() != null)
            return false;
        if (getImageInfoList() != null ? !getImageInfoList().equals(that.getImageInfoList()) : that.getImageInfoList() != null)
            return false;
        return !(getFrontCover() != null ? !getFrontCover().equals(that.getFrontCover()) : that.getFrontCover() != null);

    }

    @Override
    public int hashCode() {
        int result = getFolderName() != null ? getFolderName().hashCode() : 0;
        result = 31 * result + (getImageInfoList() != null ? getImageInfoList().hashCode() : 0);
        result = 31 * result + (getFrontCover() != null ? getFrontCover().hashCode() : 0);
        return result;
    }
}
