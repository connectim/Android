package connect.utils.data;

import connect.ui.activity.R;
import connect.ui.activity.wallet.bean.RateBean;
import connect.ui.base.BaseApplication;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * currency tool
 * Created by Administrator on 2017/1/17.
 */

public class RateDataUtil {

    private static RateDataUtil rateUtil = null;

    public static RateDataUtil getInstance() {
        if (null == rateUtil) {
            rateUtil = new RateDataUtil();
        }
        return rateUtil;
    }

    public static final String rate_data =
            "[\n" +
                    "    {\n" +
                    "        \"code\": \"USD\",\n" +
                    "        \"url\": \"/apis/usd\",\n" +
                    "        \"symbol\": \"$\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"code\": \"CNY\",\n" +
                    "        \"url\": \"/apis/cny\",\n" +
                    "        \"symbol\": \"¥\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"code\": \"RUB\",\n" +
                    "        \"url\": \"/apis/rub\",\n" +
                    "        \"symbol\": \"\\u20BD\"\n" +
                    "    }\n" +
                    "]";

    public RateBean getRateBTC() {
        RateBean rateBean = new RateBean();
        rateBean.setCode("BTC");
        rateBean.setSymbol(BaseApplication.getInstance().getString(R.string.Set_BTC_symbol));
        return rateBean;
    }

    /**
     * Access to national currency
     * @return
     */
    public ArrayList<RateBean> getRateData() {
        Type type = new TypeToken<ArrayList<RateBean>>() {}.getType();
        return new Gson().fromJson(rate_data, type);
    }

    /**
     * Access to national currency
     * @param countryCode
     * @return
     */
    public RateBean getRate(String countryCode) {
        ArrayList<RateBean> data = getRateData();
        for(RateBean rateBean : data){
            if(rateBean.getCode().equals(countryCode)){
                return rateBean;
            }
        }
        return null;
    }

}
