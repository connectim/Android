package connect.database.green.DaoHelper;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import connect.activity.set.bean.SystemSetBean;
import connect.database.green.bean.ParamEntity;

/**
 * Created by Administrator on 2017/1/8.
 */
public class ParamManager {

    private static ParamManager paramManager;
    /** wallet set */
    public static final String WALLET_SET = "wallet_set";
    /** system set */
    public static final String SYSTEM_SET = "system_set";
    /** wallet info */
    public static final String WALLET_INFO = "wallet_info";
    /** user transfer address book */
    public static final String USER_ADDRESS_BOOK = "user_address_book";
    /** The address book version number */
    public static final String COUNT_FRIENDLIST = "COUNT_FRIENDLIST";
    /** Recent transfer record */
    public static final String LATELY_TRANSFER = "lately_transfer";
    /** The key to expand */
    public static final String GENERATE_TOKEN_SALT = "GENERATE_TOKEN_SALT";
    public static final String GENERATE_TOKEN_EXPIRED = "GENERATE_TOKEN_EXPIRED ";
    public static final String COMMONLY_SEARCH = "commonly_search";

    public static ParamManager getInstance() {
        if (paramManager == null) {
            synchronized (ParamManager.class) {
                if (paramManager == null) {
                    paramManager = new ParamManager();
                }
            }
        }
        return paramManager;
    }

    /********************************************************************************************************
     *                                          INT
     *******************************************************************************************************/
    public void putInt(String key, int value){
        ParamEntity paramEntity = new ParamEntity();
        paramEntity.setKey(key);
        paramEntity.setValue(String.valueOf(value));
        ParamHelper.getInstance().insertParamEntity(paramEntity);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int def) {
        ParamEntity paramEntity = ParamHelper.getInstance().loadParamEntity(key);
        if (paramEntity == null) return def;
        return Integer.parseInt(paramEntity.getValue());
    }


    /********************************************************************************************************
     *                                   STRING
     *******************************************************************************************************/
    public String getString(String key) {
        return getString(key, "");
    }

    public String getString(String key, String def) {
        ParamEntity paramEntity = ParamHelper.getInstance().loadParamEntity(key);
        if (paramEntity == null) return def;
        return paramEntity.getValue();
    }

    public List<ParamEntity> getLikeParamEntities(String key) {
        return ParamHelper.getInstance().likeParamEntities(key);
    }

    public void putValue(String key, String value){
        ParamEntity paramEntity = new ParamEntity();
        paramEntity.setKey(key);
        paramEntity.setValue(value);
        ParamHelper.getInstance().insertParamEntity(paramEntity);
    }


    /********************************************************************************************************
     *                               KEY-VALUE
     *******************************************************************************************************/

    /**
     * Save the system Settings
     * @param systemSetBean
     */
    public void putSystemSet(SystemSetBean systemSetBean) {
        ParamEntity paramEntity = new ParamEntity();
        paramEntity.setKey(SYSTEM_SET);
        paramEntity.setValue(new Gson().toJson(systemSetBean));
        ParamHelper.getInstance().insertParamEntity(paramEntity);
    }

    public SystemSetBean getSystemSet() {
        ParamEntity paramEntity = ParamHelper.getInstance().loadParamEntity(SYSTEM_SET);
        if(paramEntity == null){
            SystemSetBean systemSetBean = SystemSetBean.initSystemSet();
            return systemSetBean;
        }
        return new Gson().fromJson(paramEntity.getValue(), SystemSetBean.class);
    }

    public void updateGroupApplyMember(String groupkey, int state) {
        String key = "APPLYGROUP:" + groupkey;
        List<ParamEntity> paramEntities = ParamManager.getInstance().getLikeParamEntities(key);
        if (paramEntities != null && paramEntities.size() > 0) {
            for (ParamEntity para : paramEntities) {
                para.setValue(String.valueOf(state));
            }
            ParamHelper.getInstance().updateParamEntities(paramEntities);
        }
    }

    public void putCommonlyString(String value) {
        ArrayList<String> list = getCommonlySearch();
        if(list.contains(value)){
            list.remove(value);
        }
        if(list.size() > 9){
            for(int i = 0; i < list.size(); i++){
                if(i > 9){
                    list.remove(i);
                }
            }
        }
        list.add(0, value);
        putCommonlySearch(list);
    }

    public void removeCommonlyString(String value) {
        ArrayList<String> list = getCommonlySearch();
        if(list.contains(value)){
            list.remove(value);
        }
        putCommonlySearch(list);
    }

    public void putCommonlySearch(ArrayList<String> list) {
        ParamEntity paramEntity = new ParamEntity();
        paramEntity.setKey(COMMONLY_SEARCH);
        paramEntity.setValue(new Gson().toJson(list));
        ParamHelper.getInstance().insertParamEntity(paramEntity);
    }

    public ArrayList<String> getCommonlySearch() {
        ParamEntity paramEntity = ParamHelper.getInstance().loadParamEntity(COMMONLY_SEARCH);
        if(paramEntity == null){
            return new ArrayList<>();
        }
        return new Gson().fromJson(paramEntity.getValue(), ArrayList.class);
    }

}
