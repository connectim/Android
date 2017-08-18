package connect.activity.chat.bean;

/**
 * map bean
 * Created by gtq on 2016/12/26.
 */
public class GeoAddressBean {

    private double locationLatitude;
    private double locationLongitude;
    private String address;
    private String path;
    private int imageOriginWidth;
    private int imageOriginHeight;

    public GeoAddressBean(double locationLatitude, double locationLongitude, String address, String path, int imageOriginWidth, int imageOriginHeight) {
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
        this.address = address;
        this.path = path;
        this.imageOriginWidth = imageOriginWidth;
        this.imageOriginHeight = imageOriginHeight;
    }

    public double getLocationLatitude() {
        return locationLatitude;
    }

    public double getLocationLongitude() {
        return locationLongitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getImageOriginWidth() {
        return imageOriginWidth;
    }

    public void setImageOriginWidth(int imageOriginWidth) {
        this.imageOriginWidth = imageOriginWidth;
    }

    public int getImageOriginHeight() {
        return imageOriginHeight;
    }

    public void setImageOriginHeight(int imageOriginHeight) {
        this.imageOriginHeight = imageOriginHeight;
    }
}
