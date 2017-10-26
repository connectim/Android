package connect.utils.http;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.protobuf.ByteString;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import connect.activity.base.BaseApplication;
import connect.activity.home.bean.HttpRecBean;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.utils.ConfigUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.log.LogManager;
import connect.utils.okhttp.LoggerInterceptor;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import protos.Connect;
import retrofit2.Retrofit;
import retrofit2.converter.protobuf.ProtoConverterFactory;

/**
 * Created by Administrator on 2017/9/27 0027.
 */

public class HttpUtil {

    private static HttpUtil mInstance;
    public final Retrofit retrofit;
    private Long connectTimeout = 10000L;

    public static HttpUtil getInstance() {
        if (mInstance == null) {
            synchronized (HttpUtil.class) {
                if (mInstance == null) {
                    mInstance = new HttpUtil();
                }
            }
        }
        return mInstance;
    }

    private HttpUtil(){
        // 配置OkHttp
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .addInterceptor(new LoggerInterceptor(false))
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .build();
        okHttpClient.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                request.body();
                return null;
            }
        });

        // 配置Retrofit
        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(ProtoConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(ConfigUtil.getInstance().serverAddress())
                .build();
    }

    public void request(Activity activity, Observable observable, HttpCallListener callback){
        request(activity, true, observable, callback);
    }

    public void request(Activity activity, boolean isShowProgress, Observable observable, HttpCallListener callback){
        final ProgressObserver observer = new ProgressObserver(callback);
        observer.setActivity(activity);
        observer.setShowProgress(isShowProgress);

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    /**
     * 加解密Proto
     */
    public static Connect.IMRequest getIMRequest(ByteString bytes) {
        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        return getIMRequest(userBean.getPriKey(), userBean.getPubKey(), bytes);
    }

    public static Connect.IMRequest getIMRequest(String priKey, String pubKey, ByteString bytes) {
        String index = ParamManager.getInstance().getString(ParamManager.GENERATE_TOKEN_SALT);
        if(TextUtils.isEmpty(index)){
            HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.SALTEXPIRE);
            Toast.makeText(BaseApplication.getInstance(), R.string.ErrorCode_Request_Error,Toast.LENGTH_LONG).show();
            return null;
        }
        return getIMRequest(EncryptionUtil.ExtendedECDH.SALT, priKey, pubKey, bytes);
    }

    public static Connect.IMRequest getIMRequest(EncryptionUtil.ExtendedECDH exts, String priKey, String pubKey, ByteString bytes) {
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

    /**
     * 获取Api类
     * @return
     */
    public static BaseApi getBaseApi() {
        HttpUtil httpUtil = HttpUtil.getInstance();
        BaseApi baseApi = httpUtil.retrofit.create(BaseApi.class);
        return baseApi;
    }

}
