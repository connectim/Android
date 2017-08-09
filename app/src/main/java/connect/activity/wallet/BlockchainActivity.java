package connect.activity.wallet;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.widget.TopToolBar;

/**
 * Created by Administrator on 2016/12/14.
 */
public class BlockchainActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.web_view)
    WebView webView;
    @Bind(R.id.myProgressBar)
    ProgressBar myProgressBar;

    private BlockchainActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_blockchain);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, CurrencyEnum currencyBean, String type) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("Currency", currencyBean);
        bundle.putString("id", type);
        ActivityUtil.next(activity, BlockchainActivity.class, bundle);
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Wallet_Transaction_detail);
        Bundle bundle = getIntent().getExtras();
        String id = bundle.getString("id");
        CurrencyEnum currencyBean = (CurrencyEnum) bundle.getSerializable("Currency");
        String url = currencyUrl(currencyBean, id);

        webView.loadUrl(url);
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
        webView.setWebChromeClient(new WebChromeClient() {
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
                ActivityUtil.goBack(mActivity);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public String currencyUrl(CurrencyEnum bean, String id) {
        String url = "";
        switch (bean) {
            case BTC:
                url = "https://blockchain.info/tx/" + id;
                break;
        }
        return url;
    }

}
