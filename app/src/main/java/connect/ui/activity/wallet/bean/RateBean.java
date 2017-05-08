package connect.ui.activity.wallet.bean;

/**
 * Created by Administrator on 2016/12/15.
 */
public class RateBean {

    /**
     * datetime : Dec 15, 2016 09:06:00 UTC
     * rate : 5364.1467
     * code : CNY
     */

    private String datetime;
    private Double rate;
    private String code;
    private String symbol;
    private String url;
    private String name;

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
