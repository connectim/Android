package connect.ui.activity.chat.bean;

/**
 * map bean
 * Created by gtq on 2016/12/26.
 */
public class GeoAddressBean {
    private double locationLatitude;
    private double locationLongitude;
    private String address;
    private String path;

    public GeoAddressBean() {
    }

    public GeoAddressBean(double lat, double lon, String address, String path) {
        this.locationLatitude = lat;
        this.locationLongitude = lon;
        this.address = address;
        this.path = path;
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

    public class GeoLocation{
        double locationLatitude;
        double locationLongitude;
        String address;

        public GeoLocation(double locationLatitude, double locationLongitude, String address) {
            this.locationLatitude = locationLatitude;
            this.locationLongitude = locationLongitude;
            this.address = address;
        }
    }
}
