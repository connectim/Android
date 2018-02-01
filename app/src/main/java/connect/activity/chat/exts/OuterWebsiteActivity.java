package connect.activity.chat.exts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.bean.RoomSession;
import connect.activity.home.bean.MsgNoticeBean;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.GroupEntity;
import connect.instant.model.CFriendChat;
import connect.instant.model.CGroupChat;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.dialog.DialogUtil;
import connect.utils.RegularUtil;
import connect.utils.scan.ResolveUrlUtil;
import connect.widget.TopToolBar;
import connect.widget.selefriend.SelectRecentlyChatActivity;
import instant.bean.ChatMsgEntity;
import instant.sender.model.NormalChat;

public class OuterWebsiteActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.web_view)
    WebView webView;
    @Bind(R.id.myProgressBar)
    ProgressBar myProgressBar;

    private OuterWebsiteActivity activity;
    private static String TAG = "_OuterWebsiteActivity";
    private static String URL = "URL";
    private String inUrl;
    private String title = "";
    private String subtitle = "";
    private String imgUrl = "";
    private ResolveUrlUtil resolveUrlUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outer_website);
        ButterKnife.bind(this);
        inUrl = getIntent().getStringExtra(URL);
        if (!RegularUtil.matches(inUrl, RegularUtil.VERIFICATION_URL_HEADER)) {
            inUrl = "http://" + inUrl;
        }
        initView();
    }

    public static void startActivity(Activity activity, String url) {
        Bundle bundle = new Bundle();
        bundle.putString(URL, url);
        ActivityUtil.next(activity, OuterWebsiteActivity.class, bundle);
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

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public void initView() {
        activity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setRightImg(R.mipmap.wallet_share_payment2x);
        toolbarTop.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbarTop.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<String> listArr = new ArrayList();
                listArr.add(activity.getString(R.string.Link_Share_to_Friend));
                listArr.add(activity.getString(R.string.Link_Open_in_Browser));

                DialogUtil.showBottomView(activity, listArr, new DialogUtil.DialogListItemClickListener() {
                    @Override
                    public void confirm(int position) {
                        if (activity.getResources().getString(R.string.Link_Share_to_Friend).equals(listArr.get(position))) {
                            SelectRecentlyChatActivity.startActivity(activity, SelectRecentlyChatActivity.SHARE_CARD);
                        } else if (getString(R.string.Link_Open_in_Browser).equals(listArr.get(position))) {//open browser
                            openUrlBrowser(inUrl);
                        }
                    }
                });
            }
        });

        webView = (WebView) findViewById(R.id.web_view);
        myProgressBar = (ProgressBar) findViewById(R.id.myProgressBar);
        myProgressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(inUrl)) {
            webView.loadUrl(inUrl);
        } else {
            return;
        }

        webView.canGoBack();
        webView.setDownloadListener(new MyWebViewDownLoadListener());
        webView.setWebChromeClient(MyWebChromeClient);
        webView.setWebViewClient(myWebViewClient);
        webView.addJavascriptInterface(new InJavaScriptLocalObj(), "local_obj");
        WebSettings webSettings = webView.getSettings();
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(true);
        if (Build.VERSION.SDK_INT >= 11) {
            webSettings.setPluginState(WebSettings.PluginState.ON);
            webSettings.setDisplayZoomControls(false);
        }
        resolveUrlUtil = new ResolveUrlUtil(activity);
    }

    private void openUrlBrowser(String url){
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        activity.startActivity(intent);
    }

    final class InJavaScriptLocalObj {
        @JavascriptInterface
        public void showTitle(String string) {
            title = string;
        }

        @JavascriptInterface
        public void showContent(String content) {
            if (TextUtils.isEmpty(content)) {
                subtitle = inUrl;
            } else {
                subtitle = content;
            }
        }

        @JavascriptInterface
        public void showImg(String img) {
            imgUrl = img;
        }
    }

    private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                    String mimetype, long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MsgNoticeBean notice) {
        resolveUrlUtil.showMsgTip(notice,ResolveUrlUtil.TYPE_OPEN_WEB,false);
    }

    private WebViewClient myWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            inUrl = url;
            if(RegularUtil.matches(url, resolveUrlUtil.Web_Url)){
                resolveUrlUtil.checkAppOpen(url);
                return true;
            }else{
                view.loadUrl(url);
                return false;
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.loadUrl("javascript:window.local_obj.showTitle(document.getElementsByTagName('title')[0].text);");
            view.loadUrl("javascript:window.local_obj.showContent(document.getElementsByTagName('meta')['description'].content);");
            view.loadUrl("javascript:window.local_obj.showImg(document.getElementsByTagName('img')[0].src);");
        }
    };

    private WebChromeClient MyWebChromeClient = new WebChromeClient() {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                myProgressBar.setProgress(newProgress);
                myProgressBar.setVisibility(View.GONE);
            } else {
                myProgressBar.setProgress(newProgress);
                myProgressBar.postInvalidate();
            }
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            onWebTitle(view, title);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
        }
    };

    protected void onWebTitle(WebView view, String string) {
        title = string;
        toolbarTop.setTitle(title);
    }

    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, keyEvent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SelectRecentlyChatActivity.CODE_REQUEST && resultCode == RESULT_OK) {
            int type = data.getIntExtra("type", 0);
            String pubkey = data.getStringExtra("object");

            NormalChat baseChat = null;
            if (pubkey.equals(RoomSession.getInstance().getRoomKey())) {
                MsgSend.sendOuterMsg(MsgSend.MsgSendType.OUTER_WEBSITE, inUrl, title, TextUtils.isEmpty(subtitle) ? inUrl : subtitle, imgUrl);
            } else {
                switch (type) {
                    case 0:
                        baseChat = new CFriendChat(pubkey);
                        break;
                    case 1:
                        GroupEntity group = ContactHelper.getInstance().loadGroupEntity(pubkey);
                        baseChat = new CGroupChat(group);
                        break;
                }

                ChatMsgEntity msgExtEntity = baseChat.outerWebsiteMsg(inUrl, title, TextUtils.isEmpty(subtitle) ? inUrl : subtitle, imgUrl);
                baseChat.sendPushMsg(msgExtEntity);
                MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);

                switch (type) {
                    case 0:
                        ((CFriendChat) baseChat).updateRoomMsg(null, getString(R.string.Chat_Sharelink), msgExtEntity.getCreatetime());
                        break;
                    case 1:
                        ((CGroupChat) baseChat).updateRoomMsg(null, getString(R.string.Chat_Sharelink), msgExtEntity.getCreatetime());
                        break;
                }
            }
        }
    }
}
