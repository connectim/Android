package connect.activity.set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.database.MemoryDataManager;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.system.SystemDataUtil;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;

/**
 * Created by Administrator on 2017/3/2.
 */

public class FeedBackActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.web_view)
    WebView webView;
    @Bind(R.id.myProgressBar)
    ProgressBar myProgressBar;

    private FeedBackActivity mActivity;
    private ValueCallback<Uri> mUploadMessage;
    private int FILECHOOSER_RESULTCODE = 350;
    private int FILECHOOSER_RESULTCODE_FOR_ANDROID_5 = 351;
    private ValueCallback<Uri[]> mUploadMessageForAndroid5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_feedback);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_FeedBack);
        String languageCode = SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.APP_LANGUAGE_CODE);
        webView.loadUrl("https://www.connect.im/mobile/feedback/" + MemoryDataManager.getInstance().getAddress() + "?locale=" + languageCode);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient(){
            //Android 5.0+
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                openFileChooserImplForAndroid5(filePathCallback);
                return true;
            }

            //Android 4.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                openFileChooserImpl(uploadMsg);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress >= 70) {
                    myProgressBar.setVisibility(View.GONE);
                } else {
                    if (View.GONE == myProgressBar.getVisibility()) {
                        myProgressBar.setVisibility(View.VISIBLE);
                    }
                    myProgressBar.setProgress(newProgress);
                }
            }
        });
        webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(webView.getSettings().getUserAgentString()+
                "CONNECT.IM("+ SystemDataUtil.getVersionName(mActivity) +";"+ android.os.Build.MODEL +")");
        webView.getSettings().getUserAgentString();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JavaScriptInterface(mActivity),"WebViewInterface");
    }

    private void openFileChooserImpl(ValueCallback<Uri> uploadMsg) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
    }

    private void openFileChooserImplForAndroid5(ValueCallback<Uri[]> uploadMsg) {
        mUploadMessageForAndroid5 = uploadMsg;
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("image/*");

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");

        startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE_FOR_ANDROID_5);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != RESULT_OK ? null: intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else if (requestCode == FILECHOOSER_RESULTCODE_FOR_ANDROID_5){
            if (null == mUploadMessageForAndroid5)
                return;
            Uri result = (intent == null || resultCode != RESULT_OK) ? null: intent.getData();
            if (result != null) {
                mUploadMessageForAndroid5.onReceiveValue(new Uri[]{result});
            } else {
                mUploadMessageForAndroid5.onReceiveValue(new Uri[]{});
            }
            mUploadMessageForAndroid5 = null;
        }
    }

    public class JavaScriptInterface{
        Activity activity;

        JavaScriptInterface(Activity activity) {
            this.activity = activity;
        }

        @JavascriptInterface
        public void FeedBackClose() {
            ActivityUtil.goBack(activity);
        }
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            } else {
                goback(new View(mActivity));
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
