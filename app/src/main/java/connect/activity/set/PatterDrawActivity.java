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
 * Created by Administrator on 2016/12/3.
 */
public class PatterDrawActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.id_gestureLockViewGroup)
    GestureLockViewGroup idGestureLockViewGroup;
    @Bind(R.id.drawpatter_gestop)
    GestureTopView drawpatterGestop;
    @Bind(R.id.drawpatter_tv)
    TextView drawpatterTv;
    @Bind(R.id.userpass_tv)
    TextView userpassTv;

    private PatterDrawActivity mActivity;
    public final static String TYPE_CHANGE = "chnage";
    public final static String TYPE_NEW = "new";
    public final static String TYPE_CLOSE = "close";
    private String type;

    public static void startActivity(Activity activity, String type, String patter) {
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        bundle.putString("data", patter);
        ActivityUtil.next(activity, PatterDrawActivity.class, bundle);
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

        drawpatterGestop.setChooseData(null);
        idGestureLockViewGroup.setOnGestureLockViewListener(onGestureLockViewListener);
        if(type.equals(TYPE_NEW)){
            idGestureLockViewGroup.setAnswer("","");
            idGestureLockViewGroup.setUnMatchExceedBoundary(1000);
            userpassTv.setVisibility(View.GONE);
        }else {
            UserBean userBean = new Gson().fromJson(SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.USER_INFO), UserBean.class);
            idGestureLockViewGroup.setAnswer(userBean.getPriKey(),userBean.getSalt());
            userpassTv.setVisibility(View.VISIBLE);
        }
        showErrorHint(getString(R.string.Set_Draw_your_pattern),false);
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void redoPass(View view){
        toolbarTop.setRightText("");
        idGestureLockViewGroup.setAnswer("","");
        drawpatterGestop.setChooseData(null);
        showErrorHint(getString(R.string.Set_Draw_your_pattern),false);
    }

    @OnClick(R.id.userpass_tv)
    void userPass(View view) {
        LoginPassCheckUtil.getInstance().checkLoginPass(mActivity, new LoginPassCheckUtil.OnResultListence() {
            @Override
            public void success(String priKey) {
                if (type.equals(TYPE_CHANGE)) {
                    type = TYPE_NEW;
                    initView();
                } else if (type.equals(TYPE_CLOSE)) {
                    ToastEUtil.makeText(mActivity,R.string.Set_Remove_Success).show();
                    putSharedPre(priKey,"");
                }
            }

            @Override
            public void error() {

            }
        });
    }

    /**
     * Gesture input after the completion of the correction
     */
    private GestureLockViewGroup.OnGestureLockViewListener onGestureLockViewListener
            = new GestureLockViewGroup.OnGestureLockViewListener() {
        @Override
        public void onBlockSelected(int cId) {

        }

        @Override
        public void onGestureEvent(boolean matched) {
            gestureChange(matched);
        }

        @Override
        public void onUnmatchedExceedBoundary() {
            // More than maximum number of errors
            if (type.equals(TYPE_CHANGE)) {
                ToastEUtil.makeText(mActivity,R.string.Login_Password_incorrect).show();
                ActivityUtil.goBack(mActivity);
            } else if (type.equals(TYPE_CLOSE)) {
                ToastEUtil.makeText(mActivity,R.string.Login_Password_incorrect).show();
                ActivityUtil.goBack(mActivity);
            }
        }
    };

    private void gestureChange(boolean matched){
        switch (type){
            case TYPE_NEW: // set
                if (matched) {
                    drawpatterGestop.setChooseData(idGestureLockViewGroup.getMChoose());
                    ToastEUtil.makeText(mActivity,R.string.Set_Pattern_Setting_Success).show();
                    putSharedPre(idGestureLockViewGroup.getAnswer(),idGestureLockViewGroup.getSalt());
                } else {
                    String value = StringUtil.listToString(idGestureLockViewGroup.getMChoose());
                    if(TextUtils.isEmpty(idGestureLockViewGroup.getAnswer())){
                        String patterSalt = SupportKeyUril.cdSaltPri();
                        String gcmStr = SupportKeyUril.encodePri(MemoryDataManager.getInstance().getPriKey(),patterSalt,value);
                        drawpatterGestop.setChooseData(idGestureLockViewGroup.getMChoose());
                        idGestureLockViewGroup.setAnswer(gcmStr,patterSalt);
                        showErrorHint(getString(R.string.Set_Draw_pattern_again),false);
                        toolbarTop.setRightText(R.string.Set_Reset);
                    }else{
                        showErrorHint(getString(R.string.Set_Two_Patterns_do_not_match),true);
                    }
                }
                break;
            case TYPE_CHANGE: // change
                if (matched) {
                    type = TYPE_NEW;
                    initView();
                    showErrorHint(getString(R.string.Set_Enter_correct_please_enter_a_new_gesture),false);
                } else {
                    showErrorHint(getString(R.string.Set_Password_incorrect_you_have_chance,idGestureLockViewGroup.getUnMatchExceedBoundary()),true);
                }
                break;
            case TYPE_CLOSE: // closed
                if (matched) {
                    ToastEUtil.makeText(mActivity,R.string.Set_Remove_Success).show();
                    putSharedPre(idGestureLockViewGroup.getPriKey(),"");
                } else {
                    showErrorHint(getString(R.string.Set_Password_incorrect_you_have_chance,idGestureLockViewGroup.getUnMatchExceedBoundary()),true);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Passwords and encryption salt to save gestures
     * @param value
     * @param salt
     */
    private void putSharedPre(String value,String salt) {
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
     * @param resId
     * @param isError
     */
    private void showErrorHint(String resId,boolean isError){
        if(resId == null){
            drawpatterTv.setVisibility(View.INVISIBLE);
        }else{
            drawpatterTv.setVisibility(View.VISIBLE);
            drawpatterTv.setText(resId);
            if(isError){
                drawpatterTv.setTextColor(mActivity.getResources().getColor(R.color.color_ff6c5a));
                Animation animationInto = AnimationUtils.loadAnimation(mActivity,R.anim.text_shake);
                animationInto.setInterpolator(new CycleInterpolator(5));
                drawpatterTv.setAnimation(animationInto);
            }else{
                drawpatterTv.setTextColor(mActivity.getResources().getColor(R.color.color_161A21));
            }
        }
    }

}
