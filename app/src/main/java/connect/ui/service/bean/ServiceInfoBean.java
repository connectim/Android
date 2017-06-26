package connect.ui.service.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/6/22.
 */

public class ServiceInfoBean implements Serializable {

    private String serviceAddress;
    private int port;

    public ServiceInfoBean() {
    }

    public ServiceInfoBean(String serviceAddress, int port) {
        this.serviceAddress = serviceAddress;
        this.port = port;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
