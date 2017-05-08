package connect.utils.okhttp;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;

import connect.db.SharedPreferenceUtil;
import connect.ui.activity.login.bean.UserBean;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
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

    public void get(String url, final ResultCall resultCall){
        HttpRequest.getInstance().get(url,resultCall);
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
     * post(receive ByteString)
     * @param url
     * @param bytes
     * @param resultCall
     */
    public void postEncrySelf(String url, ByteString bytes, final ResultCall resultCall){
        UserBean user = SharedPreferenceUtil.getInstance().getUser();
        Connect.IMRequest imRequest = getIMRequest(user.getPriKey(),user.getPubKey(),bytes);
        if(null == imRequest)
            return;
        HttpRequest.getInstance().post(url,imRequest,resultCall);
    }

    /**
     * post(receive ProtoBuff)
     * @param url
     * @param body
     * @param exts
     * @param resultCall
     */
    public void postEncrySelf(String url, GeneratedMessageV3 body,SupportKeyUril.EcdhExts exts, final ResultCall resultCall){
        LogManager.getLogger().http("param:" + body.toString());
        UserBean user = SharedPreferenceUtil.getInstance().getUser();
        ByteString bytes = body == null ? ByteString.copyFrom(new byte[]{}) : body.toByteString();
        Connect.IMRequest imRequest = getIMRequest(exts,user.getPriKey(),user.getPubKey(),bytes);
        if(null == imRequest)
            return;
        HttpRequest.getInstance().post(url,imRequest,resultCall);
    }

    /**
     * post(receive ProtoBuff)
     * @param url
     * @param body
     * @param exts
     * @param priKey
     * @param pubKey
     * @param resultCall
     */
    public void postEncry(String url, GeneratedMessageV3 body, SupportKeyUril.EcdhExts exts, String priKey, String pubKey,final ResultCall resultCall){
        LogManager.getLogger().http("param:" + body.toString());
        ByteString bytes = body == null ? ByteString.copyFrom(new byte[]{}) : body.toByteString();
        postEncry(url,bytes,exts,priKey,pubKey,resultCall);
    }

    public void postEncry(String url, ByteString body, SupportKeyUril.EcdhExts exts, String priKey, String pubKey,final ResultCall resultCall){
        Connect.IMRequest imRequest = getIMRequest(exts,priKey,pubKey,body);
        if(null == imRequest)
            return;
        HttpRequest.getInstance().post(url,imRequest,resultCall);
    }

    /**
     * encrypt param
     * @param priKey
     * @param pubKey
     * @param bytes
     * @return
     */
    private Connect.IMRequest getIMRequest(String priKey, String pubKey, ByteString bytes) {
        return getIMRequest(SupportKeyUril.EcdhExts.SALT, priKey, pubKey, bytes);
    }

    private Connect.IMRequest getIMRequest(SupportKeyUril.EcdhExts exts, String priKey, String pubKey, ByteString bytes) {
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
