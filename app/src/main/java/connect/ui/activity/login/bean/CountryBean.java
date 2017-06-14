package connect.ui.activity.login.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/1/5.
 */
public class CountryBean implements Serializable{

    String code;
    String name;
    String countryCode;

    public CountryBean() {
    }

    public CountryBean(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
