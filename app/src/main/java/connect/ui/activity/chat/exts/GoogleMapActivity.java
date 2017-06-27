package connect.ui.activity.chat.exts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgSend;
import connect.ui.activity.chat.bean.GeoAddressBean;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.FileUtil;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;
import connect.view.TopToolBar;

/**
 * google map
 */
public class GoogleMapActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.web_view)
    WebView webView;
    @Bind(R.id.myProgressBar)
    ProgressBar myProgressBar;

    private GoogleMapActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity) {
        startActivity(activity, 0, 0);
    }

    public static void startActivity(Activity activity, double lat, double lon) {
        Bundle bundle = new Bundle();
        bundle.putDouble("LAT", lat);
        bundle.putDouble("LON", lon);
        ActivityUtil.next(activity, GoogleMapActivity.class, bundle);
    }

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public void initView() {
        activity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(getResources().getString(R.string.Chat_Loc));
        toolbarTop.setRightText(R.string.Link_Send);
        toolbarTop.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbarTop.setRightListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("javascript:geoLocation()");
            }
        });

        webView = (WebView) findViewById(R.id.web_view);
        myProgressBar = (ProgressBar) findViewById(R.id.myProgressBar);
        myProgressBar.setVisibility(View.VISIBLE);

        webView.loadUrl("file:///android_asset/google_map.html");

        webView.canGoBack();
        webView.setWebChromeClient(MyWebChromeClient);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new CallJavaScripObj(),"callJsObj");
        WebSettings webSettings = webView.getSettings();
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);
        if (Build.VERSION.SDK_INT >= 11) {
            webSettings.setPluginState(WebSettings.PluginState.ON);
            webSettings.setDisplayZoomControls(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webView.destroy();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 105://java call js
                    double lat = getIntent().getDoubleExtra("LAT", 0);
                    double lon = getIntent().getDoubleExtra("LON", 0);
                    if (lat != 0 || lon != 0) {
                        webView.loadUrl("javascript:initMarker(" + lat + "," + lon + ")");
                    }
                    break;
            }
        }
    };

    private WebChromeClient MyWebChromeClient = new WebChromeClient() {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress == 100) {
                myProgressBar.setProgress(newProgress);
                myProgressBar.setVisibility(View.GONE);
            } else {
                myProgressBar.setProgress(newProgress);
                myProgressBar.postInvalidate();
            }

            if (!handler.hasMessages(105)) {
                handler.sendEmptyMessageDelayed(105, 2000);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }
    };

    final class CallJavaScripObj {

        @JavascriptInterface
        public void showSuccessInfo(double latitude,double lontitude, String info) {
            View cv = activity.getWindow().getDecorView();
            Bitmap bitmap = Bitmap.createBitmap(cv.getWidth(), cv.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            cv.draw(canvas);
            if (null == bitmap) {
                return;
            }
            try {
                File file = FileUtil.newContactFile(FileUtil.FileType.IMG);
                FileOutputStream fos = new FileOutputStream(file);
                bitmap = Bitmap.createBitmap(bitmap, 0, SystemDataUtil.getScreenHeight() / 2 - 200, bitmap.getWidth(), SystemUtil.dipToPx(200));
                boolean b = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                GeoAddressBean addressBean = new GeoAddressBean(latitude, lontitude, info, file.getAbsolutePath());
                MsgSend.sendOuterMsg(MsgType.Location, addressBean);
                ActivityUtil.goBack(activity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void showFailInfo(String info) {
            Toast.makeText(activity,info,Toast.LENGTH_SHORT).show();
        }
    }
}
