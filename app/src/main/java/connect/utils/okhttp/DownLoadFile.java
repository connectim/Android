package connect.utils.okhttp;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * File download tool
 */
public class DownLoadFile {

    private final ResultListener resultListener;
    private Handler mDelivery = new Handler(Looper.getMainLooper());

    private String url;

    public DownLoadFile(String url, ResultListener resultListener) {
        this.url = url;
        this.resultListener = resultListener;
    }

    public void downFile() {
        Request request = new Request.Builder()
                .url(url)
                .build();
        HttpRequest.getInstance().mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                switchMain(1, 0, 0, null);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                if (response.code() == 200) {
                    byte[] dataTemp = response.body().bytes();
                    switchMain(2, 0, 0, dataTemp);
                } else {

                }
            }
        });
    }

    private void switchMain(final int status, final long bytesRead, final long contentLength, final byte[] data) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                switch (status) {
                    case 0:
                        resultListener.update(bytesRead, contentLength);
                        break;
                    case 1:
                        resultListener.fail();
                        break;
                    case 2:
                        resultListener.success(data);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public interface ResultListener {

        void update(long bytesRead, long contentLength);

        void fail();

        void success(byte[] data);
    }

}
