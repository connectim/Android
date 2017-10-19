package connect.utils.okhttp;

import android.text.TextUtils;
import android.widget.Toast;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;

import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.activity.home.bean.HttpRecBean;
import connect.activity.base.BaseApplication;
import instant.utils.cryption.EncryptionUtil;
import instant.utils.cryption.SupportKeyUril;
import connect.utils.log.LogManager;
import protos.Connect;

/**
 * OkHttp network tool
 */
public class OkHttpUtil {

    private static OkHttpUtil mInstance;

    public static OkHttpUtil getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpUtil.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpUtil();
                }
            }
        }
        return mInstance;
    }

    /**
     * post(receive ProtoBuff)
     *
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
     * post(receive ByteString)
     *
     * @param url
     * @param bytes
     * @param resultCall
     */
    public void postEncrySelf(String url, ByteString bytes, final ResultCall resultCall){
        Connect.IMRequest imRequest = getIMRequest(MemoryDataManager.getInstance().getPriKey(),
                MemoryDataManager.getInstance().getPubKey(),bytes);
        if(null == imRequest)
            return;
        HttpRequest.getInstance().post(url,imRequest,resultCall);
    }

    /**
     * post(receive ProtoBuff)
     *
     * @param url
     * @param body
     * @param exts
     * @param resultCall
     */
    public void postEncrySelf(String url, GeneratedMessageV3 body,EncryptionUtil.ExtendedECDH exts, final ResultCall resultCall){
        LogManager.getLogger().http("param:" + body.toString());
        ByteString bytes = body == null ? ByteString.copyFrom(new byte[]{}) : body.toByteString();
        Connect.IMRequest imRequest = getIMRequest(exts,MemoryDataManager.getInstance().getPriKey(),
                MemoryDataManager.getInstance().getPubKey(),bytes);
        if(null == imRequest)
            return;
        HttpRequest.getInstance().post(url,imRequest,resultCall);
    }

    /**
     * post(receive ProtoBuff)
     *
     * @param url
     * @param body
     * @param exts
     * @param priKey
     * @param pubKey
     * @param resultCall
     */
    public void postEncry(String url, GeneratedMessageV3 body, EncryptionUtil.ExtendedECDH exts, String priKey, String pubKey,final ResultCall resultCall){
        LogManager.getLogger().http("param:" + body.toString());
        ByteString bytes = body == null ? ByteString.copyFrom(new byte[]{}) : body.toByteString();
        postEncry(url,bytes,exts,priKey,pubKey,resultCall);
    }

    public void postEncry(String url, ByteString body, EncryptionUtil.ExtendedECDH exts, String priKey, String pubKey,final ResultCall resultCall){
        Connect.IMRequest imRequest = getIMRequest(exts,priKey,pubKey,body);
        if(null == imRequest)
            return;
        HttpRequest.getInstance().post(url,imRequest,resultCall);
    }

    /**
     * get request
     *
     * @param url
     * @param resultCall
     */
    public void get(String url, final ResultCall resultCall){
        HttpRequest.getInstance().get(url,resultCall);
    }

    /**
     * encrypt param
     *
     * @param priKey
     * @param pubKey
     * @param bytes
     * @return
     */
    private Connect.IMRequest getIMRequest(String priKey, String pubKey, ByteString bytes) {
        String index = ParamManager.getInstance().getString(ParamManager.GENERATE_TOKEN_SALT);
        if(TextUtils.isEmpty(index)){
            HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.SALTEXPIRE);
            Toast.makeText(BaseApplication.getInstance(), R.string.ErrorCode_Request_Error,Toast.LENGTH_LONG).show();
            return null;
        }
        return getIMRequest(EncryptionUtil.ExtendedECDH.SALT, priKey, pubKey, bytes);
    }

    private Connect.IMRequest getIMRequest(EncryptionUtil.ExtendedECDH exts, String priKey, String pubKey, ByteString bytes) {
        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(exts, priKey, bytes);
        if(null == gcmData){
            LogManager.getLogger().i("-----ecdh-----","ecdh null");
            return null;
        }
        Connect.IMRequest imRequest = Connect.IMRequest.newBuilder()
                .setPubKey(pubKey)
                .setCipherData(gcmData)
                .setSign(SupportKeyUril.signHash(priKey, gcmData.toByteArray())).build();
        return imRequest;
    }
}
