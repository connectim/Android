package connect.activity.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.widget.TextView;

import com.google.gson.Gson;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.database.MemoryDataManager;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.activity.login.bean.UserBean;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.LoginPassCheckUtil;
import connect.utils.StringUtil;
import connect.utils.ToastEUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.widget.TopToolBar;
import connect.widget.lockview.GestureLockViewGroup;
import connect.widget.lockview.GestureTopView;

/**
 * Draw gesture
 */
public class SafetyPatterDrawActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.id_gestureLockViewGroup)
    GestureLockViewGroup idGestureLockViewGroup;
    @Bind(R.id.draw_patter_gestop)
    GestureTopView drawPatterGestop;
    @Bind(R.id.draw_patter_tv)
    TextView drawPatterTv;
    @Bind(R.id.user_pass_tv)
    TextView userPassTv;

    private SafetyPatterDrawActivity mActivity;
    public final static String TYPE_CHANGE = "change";
    public final static String TYPE_NEW = "new";
    public final static String TYPE_CLOSE = "close";
    private String type;

    public static void startActivity(Activity activity, String type, String patter) {
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        bundle.putString("data", patter);
        ActivityUtil.next(activity, SafetyPatterDrawActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_drawpatter);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        type = bundle.getString("type");
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_Draw_Pattern);

        drawPatterGestop.setChooseData(null);
        idGestureLockViewGroup.setOnGestureLockViewListener(onGestureLockViewListener);
        if (type.equals(TYPE_NEW)) {
            idGestureLockViewGroup.setAnswer("","");
            idGestureLockViewGroup.setUnMatchExceedBoundary(1000);
            userPassTv.setVisibility(View.GONE);
        } else {
            UserBean userBean = new Gson().fromJson(SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.USER_INFO),
                    UserBean.class);
            idGestureLockViewGroup.setAnswer(userBean.getPriKey(),userBean.getSalt());
            userPassTv.setVisibility(View.VISIBLE);
        }
        showErrorHint(getString(R.string.Set_Draw_your_pattern),false);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void redoPass(View view){
        toolbarTop.setRightText("");
        idGestureLockViewGroup.setAnswer("","");
        drawPatterGestop.setChooseData(null);
        showErrorHint(getString(R.string.Set_Draw_your_pattern),false);
    }

    @OnClick(R.id.user_pass_tv)
    void userPass(View view) {
        LoginPassCheckUtil.getInstance().checkLoginPass(mActivity, new LoginPassCheckUtil.OnResultListener() {
            @Override
            public void success(String priKey) {
                if (type.equals(TYPE_CHANGE)) {
                    type = TYPE_NEW;
                    initView();
                } else if (type.equals(TYPE_CLOSE)) {
                    ToastEUtil.makeText(mActivity,R.string.Set_Remove_Success).show();
                    saveSharedPre(priKey,"");
                }
            }

            @Override
            public void error() {}
        });
    }

    /**
     * Gesture input after the completion of the correction.
     */
    private GestureLockViewGroup.OnGestureLockViewListener onGestureLockViewListener
            = new GestureLockViewGroup.OnGestureLockViewListener() {
        @Override
        public void onBlockSelected(int cId) {}

        @Override
        public void onGestureEvent(boolean matched) {
            switch (type){
                case TYPE_NEW:
                    dealSetPatter(matched);
                    break;
                case TYPE_CHANGE:
                    dealChangePatter(matched);
                    break;
                case TYPE_CLOSE:
                    dealClosePatter(matched);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onUnmatchedExceedBoundary() {
            // More than maximum number of errors
            if (type.equals(TYPE_CHANGE) || type.equals(TYPE_CLOSE)) {
                ToastEUtil.makeText(mActivity,R.string.Login_Password_incorrect).show();
                ActivityUtil.goBack(mActivity);
            }
        }
    };

    /**
     * Set the new gesture.
     *
     * @param isMatch The input gesture to match
     */
    private void dealSetPatter(boolean isMatch){
        if (isMatch) {
            drawPatterGestop.setChooseData(idGestureLockViewGroup.getMChoose());
            ToastEUtil.makeText(mActivity,R.string.Set_Pattern_Setting_Success).show();
            saveSharedPre(idGestureLockViewGroup.getAnswer(),idGestureLockViewGroup.getSalt());
        } else {
            String value = StringUtil.listToString(idGestureLockViewGroup.getMChoose());
            if (TextUtils.isEmpty(idGestureLockViewGroup.getAnswer())) {
                String patterSalt = SupportKeyUril.getSaltPri();
                String gcmStr = SupportKeyUril.encryptionPri(MemoryDataManager.getInstance().getPriKey(),patterSalt,value);
                drawPatterGestop.setChooseData(idGestureLockViewGroup.getMChoose());
                idGestureLockViewGroup.setAnswer(gcmStr,patterSalt);
                showErrorHint(getString(R.string.Set_Draw_pattern_again),false);
                toolbarTop.setRightText(R.string.Set_Reset);
            } else {
                showErrorHint(getString(R.string.Set_Two_Patterns_do_not_match),true);
            }
        }
    }

    /**
     * Modify the gestures.
     *
     * @param isMatch The input gesture to match
     */
    private void dealChangePatter(boolean isMatch){
        if (isMatch) {
            type = TYPE_NEW;
            initView();
            showErrorHint(getString(R.string.Set_Enter_correct_please_enter_a_new_gesture),false);
        } else {
            showErrorHint(getString(R.string.Set_Password_incorrect_you_have_chance,idGestureLockViewGroup.getUnMatchExceedBoundary()),true);
        }
    }

    /**
     * Close the hand gestures
     *
     * @param isMatch The input gesture to match
     */
    private void dealClosePatter(boolean isMatch){
        if (isMatch) {
            ToastEUtil.makeText(mActivity,R.string.Set_Remove_Success).show();
            saveSharedPre(idGestureLockViewGroup.getPriKey(),"");
        } else {
            showErrorHint(getString(R.string.Set_Password_incorrect_you_have_chance,idGestureLockViewGroup.getUnMatchExceedBoundary()),true);
        }
    }

    /**
     * Passwords and encryption salt to save gestures
     *
     * @param value private key / Encryption private key
     * @param salt Encryption of salt
     */
    private void saveSharedPre(String value, String salt) {
        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        userBean.setPriKey(value);
        if(TextUtils.isEmpty(salt)){
            userBean.setSalt("");
            SharedPreferenceUtil.getInstance().putUser(userBean);
        }else{
            userBean.setSalt(salt);
            SharedPreferenceUtil.getInstance().putUser(userBean);
        }
        ActivityUtil.goBack(mActivity);
    }

    /**
     * According to the different status display text message
     *
     * @param errorTest Error text
     * @param isErrorAnim Whether to display an error jitter animation
     */
    private void showErrorHint(String errorTest,boolean isErrorAnim) {
        if (errorTest == null) {
            drawPatterTv.setVisibility(View.INVISIBLE);
        } else {
            drawPatterTv.setVisibility(View.VISIBLE);
            drawPatterTv.setText(errorTest);
            if (isErrorAnim) {
                // The error of the action
                drawPatterTv.setTextColor(mActivity.getResources().getColor(R.color.color_ff6c5a));
                Animation animationInto = AnimationUtils.loadAnimation(mActivity,R.anim.text_shake);
                animationInto.setInterpolator(new CycleInterpolator(5));
                drawPatterTv.setAnimation(animationInto);
            } else {
                // Boot prompt
                drawPatterTv.setTextColor(mActivity.getResources().getColor(R.color.color_161A21));
            }
        }
    }

}
