package connect.utils;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.login.bean.CountryBean;
import connect.activity.wallet.bean.RateBean;
import connect.ui.activity.R;
import connect.utils.data.PhoneDataUtil;
import connect.utils.data.RateDataUtil;
import connect.utils.data.ResourceUtil;

import static org.junit.Assert.assertTrue;

/**
 * Created by Administrator on 2017/8/2 0002.
 */

public class DataUtilTest {

    @Test
    public void getCountryData() {
        List<CountryBean> listCountry =  PhoneDataUtil.getInstance().getCountryData();
        if(listCountry == null || listCountry.size() == 0){
            assertTrue(false);
        }else{
            assertTrue(true);
        }
    }

    @Test
    public void getCurrentCountryCode() {
        CountryBean countryBean =  PhoneDataUtil.getInstance().getCurrentCountryCode();
        if(countryBean == null){
            assertTrue(false);
        }else{
            assertTrue(true);
        }
    }

    @Test
    public void getRateData() {
        ArrayList<RateBean> listRate =  RateDataUtil.getInstance().getRateData();
        if(listRate == null || listRate.size() == 0){
            assertTrue(false);
        }else{
            assertTrue(true);
        }
    }

    @Test
    public void getRate() {
        String code = "CNY";
        RateBean rateBean =  RateDataUtil.getInstance().getRate(code);
        if(rateBean != null && rateBean.getCode().equals(code)){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
    }

    @Test
    public void getDrawable() {
        Drawable drawable =  ResourceUtil.getDrawable(BaseApplication.getInstance().getAppContext(), R.mipmap.album_arrow_back2x);
        if(drawable == null){
            assertTrue(false);
        }else{
            assertTrue(true);
        }
    }

    @Test
    public void getColor() {
        int color =  ResourceUtil.getColor(BaseApplication.getInstance().getAppContext(), R.color.color_007aff);
        if(color == Color.BLACK){
            assertTrue(false);
        }else{
            assertTrue(true);
        }
    }

    @Test
    public void getEmotDrawable() {
        Drawable drawable =  ResourceUtil.getEmotDrawable("emoji/activities_normal.png");
        if(drawable == null){
            assertTrue(false);
        }else{
            assertTrue(true);
        }
    }

    @Test
    public void getAssetsStream() {
        InputStream inputStream =  ResourceUtil.getAssetsStream("emoji/activities_normal.png");
        if(inputStream == null){
            assertTrue(false);
        }else{
            assertTrue(true);
        }
    }

}
