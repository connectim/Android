package connect.activity.set;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.home.bean.ConversationAction;
import connect.activity.set.bean.SystemSetBean;
import connect.activity.wallet.manager.WalletManager;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastUtil;
import connect.widget.TopToolBar;

/**
 * The user general Settings
 */
public class GeneralActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.currency_ll)
    LinearLayout currencyLl;
    @Bind(R.id.sound_tb)
    View soundTb;
    @Bind(R.id.vibrate_tb)
    View vibrateTb;
    @Bind(R.id.clear_chat_tv)
    TextView clearChatTv;
    @Bind(R.id.language_ll)
    LinearLayout languageLl;

    private GeneralActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_general);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_General);


        SystemSetBean systemSetBean = ParamManager.getInstance().getSystemSet();
        soundTb.setSelected(systemSetBean.isRing());
        vibrateTb.setSelected(systemSetBean.isVibrate());

        if (WalletManager.getInstance().isCreateWallet()) {
            currencyLl.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.sound_tb})
    public void soundCheckListener(View view) {
        boolean isSele = soundTb.isSelected();
        soundTb.setSelected(!isSele);
        SystemSetBean.putRing(!isSele);
    }

    @OnClick({R.id.vibrate_tb})
    public void vibrationCheckListener(View view) {
        boolean isSele = vibrateTb.isSelected();
        vibrateTb.setSelected(!isSele);
        SystemSetBean.putVibrate(!isSele);
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.currency_ll)
    void goCurrency(View view) {
        ActivityUtil.next(mActivity, GeneralCurrencyActivity.class);
    }

    @OnClick(R.id.language_ll)
    void goLanguage(View view) {
        ActivityUtil.next(mActivity, GeneralLanguageActivity.class);
    }

    @OnClick(R.id.clear_chat_tv)
    public void clearChatRecords() {
        ConversionHelper.getInstance().clearRooms();
        MessageHelper.getInstance().clearChatMsgs();

        ConversationAction.conversationAction.sendEvent();
        ToastUtil.getInstance().showToast(getResources().getString(R.string.Link_Delete_Successful));
    }
}
