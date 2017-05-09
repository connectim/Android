package connect.ui.activity.set;

import android.app.Activity;
import android.app.Dialog;
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
import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.StringUtil;
import connect.utils.ToastEUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.view.TopToolBar;
import connect.view.lockview.GestureLockViewGroup;
import connect.view.lockview.GestureTopView;

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
    private String patterSalt;
    private Dialog dialogPass;
    private UserBean userBean;
    private String answerPri;

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
        patterSalt = bundle.getString("data", "");
        userBean = SharedPreferenceUtil.getInstance().getUser();
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
            idGestureLockViewGroup.setAnswer("",patterSalt);
            idGestureLockViewGroup.setUnMatchExceedBoundary(1000);
            userpassTv.setVisibility(View.GONE);
        }else {
            UserBean userBean = new Gson().fromJson(SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.USER_INFO), UserBean.class);
            answerPri = userBean.getPriKey();
            idGestureLockViewGroup.setAnswer(answerPri,patterSalt);
        }
        showErrorHint(getString(R.string.Set_Draw_your_pattern),false);
    }

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
        public void onUnmatchedExceedBoundary() {//More than maximum number of errors
            if (type.equals(TYPE_CHANGE)) {
                ToastEUtil.makeText(mActivity,R.string.Login_Password_incorrect).show();
                ActivityUtil.goBack(mActivity);
            } else if (type.equals(TYPE_CLOSE)) {
                ToastEUtil.makeText(mActivity,R.string.Login_Password_incorrect).show();
                ActivityUtil.goBack(mActivity);
            }
        }
    };

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void redoPass(View view){
        toolbarTop.setRightText("");
        idGestureLockViewGroup.setAnswer("",patterSalt);
        drawpatterGestop.setChooseData(null);
        showErrorHint(getString(R.string.Set_Draw_your_pattern),false);
    }

    @OnClick(R.id.userpass_tv)
    void userPass(View view){
        dialogPass = DialogUtil.showEditView(mActivity, mActivity.getResources().getString(R.string.Set_Enter_Login_Password),
                mActivity.getResources().getString(R.string.Common_Cancel),
                mActivity.getResources().getString(R.string.Common_OK),
                mActivity.getString(R.string.Login_Password_Hint, userBean.getPassHint()), "", "", true
                , 32,new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        String priKey = DecryptionUtil.decodeTalkKey(SharedPreferenceUtil.getInstance().getUser().getTalkKey(), value);
                        if(SupportKeyUril.checkPrikey(priKey)){
                            if (type.equals(TYPE_CHANGE)) {
                                type = TYPE_NEW;
                                patterSalt = "";
                                initView();
                            } else if (type.equals(TYPE_CLOSE)) {
                                ToastEUtil.makeText(mActivity,R.string.Set_Remove_Success).show();
                                putSharedPre(priKey,"");
                                ActivityUtil.goBack(mActivity);
                            }
                        }else{
                            ToastEUtil.makeText(mActivity,R.string.Login_Password_incorrect,ToastEUtil.TOAST_STATUS_FAILE).show();
                            dialogPass.show();
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                });
    }

    private void gestureChange(boolean matched){
        if (type.equals(TYPE_NEW)) {
            if (matched) {
                drawpatterGestop.setChooseData(idGestureLockViewGroup.getMChoose());
                ToastEUtil.makeText(mActivity,R.string.Set_Pattern_Setting_Success).show();
                //String value = StringUtil.listToString(idGestureLockViewGroup.getMChoose());
                putSharedPre(idGestureLockViewGroup.getAnswer(),patterSalt);
                ActivityUtil.goBack(mActivity);
            } else {
                String value = StringUtil.listToString(idGestureLockViewGroup.getMChoose());
                if(TextUtils.isEmpty(idGestureLockViewGroup.getAnswer())){
                    patterSalt = SupportKeyUril.cdSaltPri();
                    String gcmStr = SupportKeyUril.encodePri(userBean.getPriKey(),patterSalt,value);
                    drawpatterGestop.setChooseData(idGestureLockViewGroup.getMChoose());
                    idGestureLockViewGroup.setAnswer(gcmStr,patterSalt);
                    showErrorHint(getString(R.string.Set_Draw_pattern_again),false);
                    toolbarTop.setRightText(R.string.Set_Reset);
                }else{
                    showErrorHint(getString(R.string.Set_Two_Patterns_do_not_match),true);
                }
            }
        } else if (type.equals(TYPE_CHANGE)) {
            if (matched) {
                type = TYPE_NEW;
                patterSalt = "";
                initView();
                showErrorHint(getString(R.string.Set_Enter_correct_please_enter_a_new_gesture),false);
            } else {
                showErrorHint(getString(R.string.Set_Password_incorrect_you_have_chance,idGestureLockViewGroup.getUnMatchExceedBoundary()),true);
            }
        } else if (type.equals(TYPE_CLOSE)) {
            if (matched) {
                ToastEUtil.makeText(mActivity,R.string.Set_Remove_Success).show();
                putSharedPre(idGestureLockViewGroup.getPriKey(),"");
                ActivityUtil.goBack(mActivity);
            } else {
                showErrorHint(getString(R.string.Set_Password_incorrect_you_have_chance,idGestureLockViewGroup.getUnMatchExceedBoundary()),true);
            }
        }
    }

    private void putSharedPre(String value,String salt) {
        UserBean userBean = new Gson().fromJson(SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.USER_INFO), UserBean.class);
        userBean.setPriKey(value);
        if(TextUtils.isEmpty(salt)){
            userBean.setSalt("");
            SharedPreferenceUtil.getInstance().putUser(userBean);
        }else{
            userBean.setSalt(salt);
            SharedPreferenceUtil.getInstance().putUser(userBean);
        }
    }

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
