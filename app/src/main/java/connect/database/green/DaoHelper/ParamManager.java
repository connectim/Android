package connect.database.green.DaoHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import connect.activity.login.bean.CaPubBean;
import connect.activity.set.bean.SystemSetBean;
import connect.activity.wallet.bean.AddressBean;
import connect.activity.wallet.bean.WalletBean;
import connect.activity.wallet.bean.WalletSetBean;
import connect.database.green.bean.ParamEntity;
import connect.activity.chat.bean.ApplyGroupBean;
import connect.activity.set.bean.PaySetBean;
import connect.activity.set.bean.PrivateSetBean;
import connect.activity.wallet.bean.RateBean;
import connect.activity.wallet.bean.TransferBean;

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
     * Save the wallet set
     * @param walletSetBean
     */
    public void putWalletSet(WalletSetBean walletSetBean) {
        ParamEntity paramEntity = new ParamEntity();
        paramEntity.setKey(WALLET_SET);
        paramEntity.setValue(new Gson().toJson(walletSetBean));
        ParamHelper.getInstance().insertParamEntity(paramEntity);
    }

    public WalletSetBean getWalletSet() {
        ParamEntity paramEntity = ParamHelper.getInstance().loadParamEntity(WALLET_SET);
        if(paramEntity == null){
            WalletSetBean walletSetBean = WalletSetBean.initWalletSet();
            return walletSetBean;
        }
        return new Gson().fromJson(paramEntity.getValue(), WalletSetBean.class);
    }

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

    /**
     * Save the transfer address book
     * @param list
     */
    public void putTransferAddressBook(ArrayList<AddressBean> list) {
        ParamEntity paramEntity = new ParamEntity();
        paramEntity.setKey(USER_ADDRESS_BOOK);
        paramEntity.setValue(new Gson().toJson(list));
        ParamHelper.getInstance().insertParamEntity(paramEntity);
    }

    public ArrayList<AddressBean> getTransferAddressBook() {
        ParamEntity paramEntity = ParamHelper.getInstance().loadParamEntity(USER_ADDRESS_BOOK);
        if(paramEntity == null){
            return null;
        }
        Type type = new TypeToken<ArrayList<AddressBean>>() {}.getType();
        return new Gson().fromJson(paramEntity.getValue(), type);
    }

    /**
     * Save the wallet BaseSeed information
     * @param walletBean
     */
    public void putWalletInfo(WalletBean walletBean) {
        ParamEntity paramEntity = new ParamEntity();
        paramEntity.setKey(WALLET_INFO);
        paramEntity.setValue(new Gson().toJson(walletBean));
        ParamHelper.getInstance().insertParamEntity(paramEntity);
    }

    public WalletBean getWalletInfo() {
        ParamEntity paramEntity = ParamHelper.getInstance().loadParamEntity(WALLET_INFO);
        if(paramEntity == null){
            return new WalletBean();
        }
        return new Gson().fromJson(paramEntity.getValue(), WalletBean.class);
    }

    /**
     * Recent transfer record
     * @param privateSetBean
     */
    public void putLatelyTransfer(TransferBean privateSetBean) {
        ArrayList<TransferBean> list = getLatelyTransfer();
        if(list.size() >= 10){
            list.remove(9);
        }
        list.add(0,privateSetBean);
        ParamEntity paramEntity = new ParamEntity();
        paramEntity.setKey(LATELY_TRANSFER);
        paramEntity.setValue(new Gson().toJson(list));
        ParamHelper.getInstance().insertParamEntity(paramEntity);
    }

    public ArrayList<TransferBean> getLatelyTransfer() {
        ParamEntity paramEntity = ParamHelper.getInstance().loadParamEntity(LATELY_TRANSFER);
        if(paramEntity == null){
            return new ArrayList();
        }
        Type type = new TypeToken<ArrayList<TransferBean>>() {}.getType();
        return new Gson().fromJson(paramEntity.getValue(), type);
    }


    public ApplyGroupBean loadGroupApply(String verifycode) {
        ParamEntity entity = ParamHelper.getInstance().loadParamEntity(verifycode);
        if (entity == null || entity.getValue() == null) {
            return null;
        }
        return new Gson().fromJson(entity.getValue(), ApplyGroupBean.class);
    }

    public void updateGroupApply(String verifycode, String tips, int source, int state,String msgid) {
        ApplyGroupBean repeatBean = new ApplyGroupBean(tips, source, state, msgid);
        ParamEntity entity = ParamHelper.getInstance().loadParamEntity(verifycode);
        if (entity == null) {
            entity = new ParamEntity();
            entity.setKey(verifycode);
        }
        entity.setValue(new Gson().toJson(repeatBean));
        ParamHelper.getInstance().insertParamEntity(entity);
    }

    public int loadGroupApplyMember(String groupkey, String msgid) {
        String key = "APPLYGROUP:" + groupkey + msgid;
        ParamEntity param = ParamHelper.getInstance().loadParamEntityKeyExt(key, msgid);

        int state = 0;
        if (param == null) {
            param = new ParamEntity();
            param.setKey(key);
            param.setValue("0");
            param.setExt(msgid);
            ParamHelper.getInstance().insertOrReplaceParamEntity(param);
        } else {
            state = Integer.parseInt(param.getValue());
        }
        return state;
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

}
