package connect.database.green.DaoHelper;

import org.junit.Test;

import java.util.List;

import connect.activity.home.bean.RoomAttrBean;

/**
 * Created by Administrator on 2017/10/24.
 */

public class ConversionHelperTest {

    private String Tag = "_ConversionHelperTest";

    @Test
    public int countUnReads() {
        int unreadCount = ConversionHelper.getInstance().countUnReads();
        return unreadCount;
    }

    @Test
    public List<RoomAttrBean> loadRoomEntities(String identifier) {
        List<RoomAttrBean> attrBeanList=ConversionHelper.getInstance().loadRoomEntities(identifier);
        return attrBeanList;
    }

    @Test
    public List<RoomAttrBean> loadRecentRoomEntities() {
        List<RoomAttrBean> attrBeanList=ConversionHelper.getInstance().loadRecentRoomEntities();
        return attrBeanList;
    }

}
