package connect.utils.okhttp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import com.google.protobuf.GeneratedMessageV3;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import connect.ui.activity.R;
import connect.ui.activity.home.bean.HttpRecBean;
import connect.ui.base.BaseApplication;
import connect.utils.ConfigUtil;
import connect.utils.ProgressUtil;
import connect.utils.ToastEUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.log.LogManager;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
     *
     * @param url
     * @param callBack
     */
    public void get(String url, final okhttp3.Callback callBack) {
        getAbsolute(getAbsoluteUrl(url), callBack);
    }

    /**
     * get Request (return to the original data)
     *
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
                String errorNet = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Network_connection_failed_please_check_network);
                ToastUtil.getInstance().showToast(errorNet);
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
     * get Request(return ProtoBuff）
     *
     * @param url
     * @param resultCall
     */
    public void get(String url, final ResultCall resultCall) {
        if (!HttpRequest.isConnectNet()) {
            ToastUtil.getInstance().showToast(R.string.Chat_Network_connection_failed_please_check_network);
            return;
        }

        Request request = new Request.Builder()
                .url(getAbsoluteUrl(url))
                .get()
                .build();
        mOkHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ProgressUtil.getInstance().dismissProgress();
                String errorNet = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Network_connection_failed_please_check_network);
                ToastUtil.getInstance().showToast(errorNet);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    Integer code = resultCall.parseNetworkResponse(response);
                    sendResultCallback(code, resultCall);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Post Request(ProtoBuff param)
     *
     * @param url
     * @param body
     * @param resultCall
     */
    public void post(String url, GeneratedMessageV3 body, final ResultCall resultCall) {
        post(url, body.toByteArray(), resultCall);
    }

    /**
     * Post Request(byte[] param)
     *
     * @param url
     * @param content
     * @param resultCall
     */
    public void post(String url, byte[] content, final ResultCall resultCall) {
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_DEFAULT, content);
        Request request = new Request.Builder()
                .url(getAbsoluteUrl(url))
                .post(requestBody)
                .build();

        mOkHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ProgressUtil.getInstance().dismissProgress();

                resultCall.onError();
                String errorNet = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Network_connection_failed_please_check_network);
                ToastUtil.getInstance().showToast(errorNet);
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
                } else if (code == 2001) {//salt timeout
                    HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.SALTEXPIRE);
                } else {
                    resultCall.onError(resultCall.getData());
                }
            }
        });
    }

    /**
     * default Host
     *
     * @param url
     * @return
     */
    private String getAbsoluteUrl(String url) {
        return ConfigUtil.getInstance().serverAddress() + url;
    }

    /**
     * network environment
     *
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
