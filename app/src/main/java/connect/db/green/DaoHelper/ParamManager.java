package connect.db.green.DaoHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import connect.db.green.bean.ParamEntity;
import connect.ui.activity.chat.bean.ApplyGroupBean;
import connect.ui.activity.set.bean.PaySetBean;
import connect.ui.activity.set.bean.PrivateSetBean;
import connect.ui.activity.wallet.bean.RateBean;
import connect.ui.activity.wallet.bean.TransferBean;
import connect.ui.activity.wallet.bean.WalletAccountBean;

/**
 * Created by Administrator on 2017/1/8.
 */
public class ParamManager {

    private static ParamManager patamManager;

    /** The current state information */
    public static final String COUNTRY_RATE = "country_rate";
    /** Users pay set */
    public static final String USER_PAY_SET = "user_pay_set";
    /** User privacy Settings */
    public static final String USER_PRIVATE_SET = "user_private_set";
    /** The address book version number */
    public static final String COUNT_FRIENDLIST = "COUNT_FRIENDLIST";
    /** Chat set up the voice*/
    public static final String SET_VOICE = "SET_VOICE";
    /** Chat set vibration */
    public static final String SET_VIBRATION = "SET_VIBRATION";
    /** Recent transfer record */
    public static final String LATELY_TRANSFER = "lately_transfer";

    /** The key to expand */
    public static final String GENERATE_TOKEN_SALT = "GENERATE_TOKEN_SALT";
    public static final String GENERATE_TOKEN_EXPIRED = "GENERATE_TOKEN_EXPIRED ";
    /** The wallet balance */
    public static final String WALLET_AMOUNT = "wallet_amount";
    /** app join in group state 0:not deal 1:agree 2:refuse */
    public static final String GROUP_JOIN = "GROUP_JOIN";

    public static ParamManager getInstance() {
        if (patamManager == null) {
            patamManager = new ParamManager();
        }
        return patamManager;
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
    public void putPaySet(PaySetBean user) {
        ParamEntity paramEntity = new ParamEntity();
        paramEntity.setKey(USER_PAY_SET);
        paramEntity.setValue(new Gson().toJson(user));
        ParamHelper.getInstance().insertParamEntity(paramEntity);
    }

    public PaySetBean getPaySet() {
        ParamEntity paramEntity = ParamHelper.getInstance().loadParamEntity(USER_PAY_SET);
        if(paramEntity == null){
            return null;
        }
        return new Gson().fromJson(paramEntity.getValue(), PaySetBean.class);
    }

    public void putPrivateSet(PrivateSetBean privateSetBean) {
        ParamEntity paramEntity = new ParamEntity();
        paramEntity.setKey(USER_PRIVATE_SET);
        paramEntity.setValue(new Gson().toJson(privateSetBean));
        ParamHelper.getInstance().insertParamEntity(paramEntity);
    }

    public PrivateSetBean getPrivateSet() {
        ParamEntity paramEntity = ParamHelper.getInstance().loadParamEntity(USER_PRIVATE_SET);
        if(paramEntity == null){
            return null;
        }
        return new Gson().fromJson(paramEntity.getValue(), PrivateSetBean.class);
    }

    public void putWalletAmount(WalletAccountBean accountBean) {
        ParamEntity paramEntity = new ParamEntity();
        paramEntity.setKey("wallet_amount");
        paramEntity.setValue(new Gson().toJson(accountBean));
        ParamHelper.getInstance().insertParamEntity(paramEntity);
    }

    public WalletAccountBean getWalletAmount() {
        ParamEntity paramEntity = ParamHelper.getInstance().loadParamEntity("wallet_amount");
        if(paramEntity == null){
            return null;
        }
        return new Gson().fromJson(paramEntity.getValue(), WalletAccountBean.class);
    }

    public void putCountryRate(RateBean rateBean) {
        if(null != rateBean){
            ParamEntity paramEntity = new ParamEntity();
            paramEntity.setKey(COUNTRY_RATE);
            paramEntity.setValue(new Gson().toJson(rateBean));
            ParamHelper.getInstance().insertParamEntity(paramEntity);
        }
    }

    public RateBean getCountryRate() {
        ParamEntity paramEntity = ParamHelper.getInstance().loadParamEntity(COUNTRY_RATE);
        if(null == paramEntity){
            RateBean rateBean = new RateBean();
            rateBean.setCode("USD");
            rateBean.setSymbol("$");
            rateBean.setUrl("/apis/usd");
            return rateBean;
        }
        return new Gson().fromJson(paramEntity.getValue(), RateBean.class);
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

}
