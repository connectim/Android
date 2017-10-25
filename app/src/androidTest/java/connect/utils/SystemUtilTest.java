package connect.utils;

import android.text.TextUtils;

import org.junit.Test;

import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.contact.bean.PhoneContactBean;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;

import static org.junit.Assert.assertTrue;

/**
 * Created by Administrator on 2017/8/1 0001.
 */

public class SystemUtilTest {

    private String Tag = "_SystemUtilTest";

    @Test
    public void getVersionName() throws Exception {
        String version = SystemDataUtil.getVersionName(BaseApplication.getInstance().getBaseContext());
        assertTrue(!TextUtils.isEmpty(version));
    }

    @Test
    public void getDeviceId() throws Exception {
        String version = SystemDataUtil.getDeviceId();
        assertTrue(!TextUtils.isEmpty(version));
    }

    @Test
    public void getLocalUid() throws Exception {
        String uid = SystemDataUtil.getLocalUid();
        assertTrue(!TextUtils.isEmpty(uid));
    }

    @Test
    public void setAppLanguage() throws Exception {
        SystemDataUtil.setAppLanguage(BaseApplication.getInstance().getBaseContext(),"zh");
    }

    @Test
    public void getLoadAddresSbook() throws Exception {
        // permission allow
        List<PhoneContactBean> listPhone = SystemDataUtil.getLoadAddresSbook(BaseApplication.getInstance().getBaseContext());
        if(listPhone == null || listPhone.size() == 0){
            assertTrue(false);
        }else{
            assertTrue(true);
        }
    }

    @Test
    public void dipToPx() throws Exception {
        int dp = 10;
        int px = SystemUtil.dipToPx(dp);
        int dpValue = SystemUtil.pxToDip(px);
        if(dp == dpValue){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
    }

    @Test
    public void pxToSp() throws Exception {
        int px = 10;
        float sp = SystemUtil.pxToSp(px);
        int pxValue = SystemUtil.spToPx(sp);
        if(px == pxValue){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
    }

    @Test
    public void isRunBackGround() throws Exception {
        SystemUtil.isRunBackGround();
    }

    @Test
    public void isOpenWifi() throws Exception {
        SystemUtil.isOpenWifi();
    }

}
