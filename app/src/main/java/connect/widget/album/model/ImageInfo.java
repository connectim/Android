package connect.widget.album.model;

import java.io.Serializable;

/**
 * Picture information
 * <p/>
 * Created by Clock on 2016/1/26.
 */
public class ImageInfo implements Serializable {

    private static final long serialVersionUID = -3753345306395582567L;
    /** Image files */
    private ExFile imageFile;
    /** If the selected */
    private boolean isSelected = false;
    /** File type 0:picture 1:video */
    private int fileType;

    public ImageInfo(ExFile imageFile, int fileType) {
        this.imageFile = imageFile;
        this.fileType = fileType;
    }

    public ExFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(ExFile imageFile) {
        this.imageFile = imageFile;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageInfo imageInfo = (ImageInfo) o;

        if (isSelected() != imageInfo.isSelected()) return false;
        return getImageFile().equals(imageInfo.getImageFile());

    }

    @Override
    public int hashCode() {
        int result = getImageFile().hashCode();
        result = 31 * result + (isSelected() ? 1 : 0);
        return result;
    }
}
