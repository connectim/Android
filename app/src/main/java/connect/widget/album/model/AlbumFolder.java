package connect.widget.album.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * catalog information
 */
public class AlbumFolder implements Serializable {

    private AlbumFolderType albumFolderType;
    private String folderName;
    private ArrayList<AlbumFile> albumFiles = new ArrayList<>();

    public AlbumFolder(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public AlbumFolderType getAlbumFolderType() {
        return albumFolderType;
    }

    public void setAlbumFolderType(AlbumFolderType albumFolderType) {
        this.albumFolderType = albumFolderType;
    }

    public ArrayList<AlbumFile> getAlbumFiles() {
        return albumFiles;
    }

    public void addAlbumFile(AlbumFile albumFile) {
        if (!albumFiles.contains(albumFile)) {
            albumFiles.add(albumFile);
        }
    }
}
