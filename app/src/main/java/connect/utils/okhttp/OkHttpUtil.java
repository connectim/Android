package connect.utils.okhttp;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;

import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.log.LogManager;
import connect.utils.system.SystemDataUtil;
import protos.Connect;

/**
 * OkHttp network tool
 */
public class OkHttpUtil {

    private static OkHttpUtil mInstance;

    public synchronized static OkHttpUtil getInstance() {
        if (mInstance == null) {
            mInstance = new OkHttpUtil();
        }
        return mInstance;
    }

    /**
     * post(receive ProtoBuff)
     * @param url
     * @param body
     * @param resultCall
     */
    public void postEncrySelf(String url, GeneratedMessageV3 body, final ResultCall resultCall){
        LogManager.getLogger().http("param:" + body.toString());
        ByteString bytes = body == null ? ByteString.copyFrom(new byte[]{}) : body.toByteString();
        postEncrySelf(url,bytes,resultCall);
    }

    /**
     * post(receive ProtoBuff)
     * @param url
     * @param body
     * @param exts
     * @param resultCall
     */
    public void postEncrySelf(String url, GeneratedMessageV3 body, EncryptionUtil.ExtendedECDH exts, final ResultCall resultCall){
        ByteString bytes = body == null ? ByteString.copyFrom(new byte[]{}) : body.toByteString();
        postEncrySelf(url, bytes, resultCall);
        /*UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        ByteString bytes = body == null ? ByteString.copyFrom(new byte[]{}) : body.toByteString();
        Connect.HttpRequest httpRequest = getHttpRequest(exts, userBean.getPriKey(),userBean.getPubKey(), userBean.getUid(), bytes);
        if(null == httpRequest)
            return;
        HttpRequest.getInstance().post(url,httpRequest,resultCall);*/
    }

    /**
     * post(receive ByteString)
     * @param url
     * @param bytes
     * @param resultCall
     */
    public void postEncrySelf(String url, ByteString bytes, final ResultCall resultCall){
        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        Connect.HttpRequest httpRequest = Connect.HttpRequest.newBuilder()
                .setUid(userBean.getUid())
                .setBody(bytes)
                .setToken(userBean.getToken()).build();
        HttpRequest.getInstance().post(url, httpRequest, resultCall);
        /*UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        String index = ParamManager.getInstance().getString(ParamManager.GENERATE_TOKEN_SALT);
        if (TextUtils.isEmpty(index)) {
            HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.SALTEXPIRE);
            Toast.makeText(BaseApplication.getInstance(), R.string.ErrorCode_Request_Error,Toast.LENGTH_LONG).show();
        } else {
            Connect.HttpRequest httpRequest = getHttpRequest(EncryptionUtil.ExtendedECDH.SALT, userBean.getPriKey(), userBean.getPubKey(),
                    userBean.getUid(), bytes);
            HttpRequest.getInstance().post(url, httpRequest, resultCall);
        }*/
    }

    private Connect.HttpRequest getHttpRequest(EncryptionUtil.ExtendedECDH exts, String priKey, String pubKey,String uid, ByteString bytes){
        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(exts, priKey, bytes);
        if(null == gcmData){
            return null;
        }
        Connect.HttpRequest httpRequest = Connect.HttpRequest.newBuilder()
                .setUid(uid)
                .setBody(bytes)
                .setToken("").build();
        return httpRequest;
    }

    public Connect.IMRequest getIMRequest(EncryptionUtil.ExtendedECDH exts, String priKey, String pubKey, ByteString bytes) {
        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(exts, priKey, bytes);
        if(null == gcmData){
            return null;
        }
        Connect.IMRequest imRequest = Connect.IMRequest.newBuilder()
                .setPubKey(pubKey)
                .setCipherData(gcmData)
                .setSign(SupportKeyUril.signHash(priKey, gcmData.toByteArray())).build();
        return imRequest;
    }

}
