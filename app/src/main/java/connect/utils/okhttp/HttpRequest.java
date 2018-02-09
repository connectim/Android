package connect.utils.okhttp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.common.server.response.FastJsonResponse;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import connect.activity.home.bean.HomeAction;
import connect.activity.login.LoginUserActivity;
import connect.ui.activity.R;
import connect.activity.home.bean.HttpRecBean;
import connect.activity.base.BaseApplication;
import connect.utils.ConfigUtil;
import connect.utils.ProgressUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.log.LogManager;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;

/**
 * Http Request
 */

public class HttpRequest {

    private static HttpRequest mInstance;
    public final OkHttpClient mOkHttpClient;
    //The default data type
    private final MediaType MEDIA_TYPE_DEFAULT = MediaType.parse("application/octet-stream; charset=utf-8");
    private final Handler mDelivery;

    private HttpRequest() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .writeTimeout(10000L, TimeUnit.MILLISECONDS)
                .addInterceptor(new LoggerInterceptor(false))
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
        mOkHttpClient = builder.build();
        mDelivery = new Handler(Looper.getMainLooper());
    }

    public static HttpRequest getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpUtil.class) {
                if (mInstance == null) {
                    mInstance = new HttpRequest();
                }
            }
        }
        return mInstance;
    }

    /**
     * get Request (with the prefix names)
     * @param url
     * @param callBack
     */
    public void get(String url, final okhttp3.Callback callBack) {
        getAbsolute(ConfigUtil.getInstance().serverAddress() + url, callBack);
    }

    /**
     * get Request (return to the original data)
     * @param url
     * @param callBack
     */
    public void getAbsolute(String url, final okhttp3.Callback callBack) {
        if (!HttpRequest.isConnectNet()) {
            ToastUtil.getInstance().showToast(R.string.Chat_Network_connection_failed_please_check_network);
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        mOkHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ProgressUtil.getInstance().dismissProgress();
                callBack.onFailure(call, e);
                dealOnFailure(call);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                mDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            callBack.onResponse(call, response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    /**
     * Post Request(ProtoBuff param)
     * @param url
     * @param body
     * @param resultCall
     */
    public void post(String url, GeneratedMessageV3 body, final ResultCall resultCall) {
        post(url, body.toByteArray(), resultCall);
    }

    public void post(String url, ByteString bytes, final ResultCall resultCall) {
        post(url, bytes.toByteArray(), resultCall);
    }

    /**
     * Post Request(byte[] param)
     * @param url
     * @param content
     * @param resultCall
     */
    public void post(String url, byte[] content, final ResultCall resultCall) {
        if (!HttpRequest.isConnectNet()) {
            ToastUtil.getInstance().showToast(R.string.Chat_Network_connection_failed_please_check_network);
            return;
        }
        String address;
        if(url.equals(UriUtil.CONNECT_V3_PROXY_VISITOR_RECORDS) ||
                url.equals(UriUtil.CONNECT_V3_PROXY_RECORDS_HISTORY) ||
                url.equals(UriUtil.CONNECT_V3_PROXY_EXAMINE_VERIFY) ||
                url.equals(UriUtil.CONNECT_V3_PROXY_TOKEN)){
            address = ConfigUtil.getInstance().visitorAddress() + url;
        }else if(url.equals(UriUtil.STORES_V1_IWORK_LOGS) ||
                url.equals(UriUtil.STORES_V1_IWORK_LOG_COMFIRM) ||
                url.equals(UriUtil.STORES_V1_IWORK_LOGS_DETAIL)){
            address = ConfigUtil.getInstance().warehouseAddress() + url;
        }else{
            address = ConfigUtil.getInstance().serverAddress() + url;
        }

        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_DEFAULT, content);
        Request request = new Request.Builder()
                .url(address)
                .post(requestBody)
                .build();

        mOkHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ProgressUtil.getInstance().dismissProgress();
                mDelivery.post(new Runnable() {
                                   @Override
                                   public void run() {
                                       resultCall.onError();
                                   }
                               });
                dealOnFailure(call);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    Integer code = resultCall.parseNetworkResponse(response);
                    sendResultCallback(code, resultCall);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void postUploadFile(String url, byte[] content, final ResultCall resultCall) {
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_DEFAULT, content);
        Request request = new Request.Builder()
                .url("http://192.168.40.4:10086" + url)
                .post(requestBody)
                .build();

        mOkHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ProgressUtil.getInstance().dismissProgress();
                resultCall.onError();
                dealOnFailure(call);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    Integer code = resultCall.parseNetworkResponse(response);
                    sendResultCallback(code, resultCall);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendResultCallback(final Integer code, final ResultCall resultCall) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                LogManager.getLogger().i(LoggerInterceptor.TAG + "result:", "code=" + code + "," + resultCall.getData());
                if (code == 2000) {
                    resultCall.onResponse(resultCall.getData());
                } else if (code == 2001) {
                    //salt timeout
                    HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.SALTEXPIRE);
                } else if(code == 2401){
                    // sign error
                    ToastUtil.getInstance().showToast(R.string.Set_Load_failed_please_try_again_later);
                } else if(code == 2420){
                    // uid/pubKey error
                    ToastUtil.getInstance().showToast(R.string.Set_Load_failed_please_try_again_later);
                } else if(code == 2700){
                    //HomeAction.getInstance().sendEvent(HomeAction.HomeType.DELAY_EXIT);
                    ToastUtil.getInstance().showToast(R.string.Set_Load_failed_please_try_again_later);
                } else{
                    resultCall.onError(resultCall.getData());
                }
            }
        });
    }

    /**
     * Network access failure
     * @param call
     */
    private void dealOnFailure(Call call){
        try {
            switch (call.execute().code()){
                case 404:
                    ToastUtil.getInstance().showToast(R.string.Set_Load_failed_please_try_again_later);
                    break;
                default:
                    ToastUtil.getInstance().showToast(R.string.Chat_Network_connection_failed_please_check_network);
                    break;
            }
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    /**
     * network environment
     * @return
     */
    public static boolean isConnectNet() {
        Context context = BaseApplication.getInstance().getBaseContext();
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();

        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn;
        if (null != networkInfo) {
            isMobileConn = networkInfo.isConnected();
        } else {
            isMobileConn = false;
        }
        return isWifiConn || isMobileConn;
    }
}
