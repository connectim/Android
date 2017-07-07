package connect.ui.activity.locmap.bean;

/**
 * map bean
 * Created by gtq on 2016/12/26.
 */
public class GeoAddressBean {
    private double locationLatitude;
    private double locationLongitude;
    private String address;
    private String path;
    private float imageOriginWidth;
    private float imageOriginHeight;

    public GeoAddressBean() {
    }

    public GeoAddressBean(double locationLatitude, double locationLongitude, String address, String path, float imageOriginWidth, float imageOriginHeight) {
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

    public float getImageOriginWidth() {
        return imageOriginWidth;
    }

    public void setImageOriginWidth(float imageOriginWidth) {
        this.imageOriginWidth = imageOriginWidth;
    }

    public float getImageOriginHeight() {
        return imageOriginHeight;
    }

    public void setImageOriginHeight(float imageOriginHeight) {
        this.imageOriginHeight = imageOriginHeight;
    }
}
