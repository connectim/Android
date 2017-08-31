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

        int soundValue = ParamManager.getInstance().getInt(ParamManager.SET_VOICE, 1);
        soundTb.setSelected(soundValue != 0);
        int vibrateValue = ParamManager.getInstance().getInt(ParamManager.SET_VIBRATION, 1);
        vibrateTb.setSelected(vibrateValue != 0);
    }

    @OnClick({R.id.sound_tb})
    public void soundCheckListener(View view) {
        boolean isSele = soundTb.isSelected();
        soundTb.setSelected(!isSele);
        ParamManager.getInstance().putValue(ParamManager.SET_VOICE, !isSele ? "1" : "0");
    }

    @OnClick({R.id.vibrate_tb})
    public void vibrationCheckListener(View view) {
        boolean isSele = vibrateTb.isSelected();
        vibrateTb.setSelected(!isSele);
        ParamManager.getInstance().putValue(ParamManager.SET_VIBRATION, !isSele ? "1" : "0");
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
