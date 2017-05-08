package connect.view.album.entity;

import java.io.File;
import java.net.URI;

/**
 * Extended file attributes
 * Created by pujin on 2017/3/27.
 */
public class ExFile extends File {
    private int folderType;//0:image 1:video
    private long videoLength;

    public ExFile(String path) {
        super(path);
    }

    public ExFile(String path, int folderType) {
        super(path);
        this.folderType = folderType;
    }

    public ExFile(String dirPath, String name) {
        super(dirPath, name);
    }

    public ExFile(URI uri) {
        super(uri);
    }

    public int getFolderType() {
        return folderType;
    }

    public void setFolderType(int folderType) {
        this.folderType = folderType;
    }

    public long getVideoLength() {
        return videoLength;
    }

    public void setVideoLength(long videoLength) {
        this.videoLength = videoLength;
    }
}
