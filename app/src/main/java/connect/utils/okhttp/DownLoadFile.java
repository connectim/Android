package connect.utils.okhttp;

import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
                resultFail();
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                if (response.code() == 200) {
                    InputStream inputStream = null;
                    ByteArrayOutputStream outputStream = null;
                    try {
                        long current = 0;
                        int length = 0;
                        long total = response.body().contentLength();
                        byte[] buffer = new byte[1024];

                        outputStream = new ByteArrayOutputStream();
                        inputStream = response.body().byteStream();
                        while ((length = inputStream.read(buffer)) != -1) {
                            current += length;
                            outputStream.write(buffer, 0, length);
                            resultProgress(current, total);
                        }
                        outputStream.flush();
                        resultSuccess(outputStream.toByteArray());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                            inputStream = null;
                        }
                        if (outputStream != null) {
                            outputStream.close();
                            outputStream = null;
                        }
                    }
                } else {
                    resultFail();
                }
            }
        });
    }

    public void resultFail(){
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                resultListener.fail();
            }
        });
    }

    public void resultSuccess(final byte[] bytes) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                resultListener.success(bytes);
            }
        });
    }

    public void resultProgress(final long current, final long total){
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                resultListener.update(current,total);
            }
        });
    }

    public interface ResultListener {

        void update(long bytesRead, long contentLength);

        void fail();

        void success(byte[] data);
    }

}
