package connect.activity.login.bean;

import java.util.List;

public class StartImagesBean {

    String hash;
    List<String> images;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
