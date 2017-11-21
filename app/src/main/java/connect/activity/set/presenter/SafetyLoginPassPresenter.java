package connect.activity.set.presenter;

import android.text.TextUtils;

import connect.activity.set.contract.SafetyLoginPassContract;
import connect.ui.activity.R;
import connect.utils.ExCountDownTimer;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

public class SafetyLoginPassPresenter implements SafetyLoginPassContract.Presenter {

    private SafetyLoginPassContract.View mView;
    private String token;

    public SafetyLoginPassPresenter(SafetyLoginPassContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {}

    @Override
    public void requestPassword(String password, String code, final int type) {
        if(TextUtils.isEmpty(token))
            return;
        Connect.Mfa mfa = Connect.Mfa.newBuilder()
                .setTyp(type)
                .setToken(token)
                .setCode(code)
                .setVal(password)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.V2_SRTTING_PASSWORD_UPDATE, mfa, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                mView.modifySuccess(type);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if(response.getCode() == 2409){
                    ToastEUtil.makeText(mView.getActivity(), R.string.Login_Verification_code_error, ToastEUtil.TOAST_STATUS_FAILE).show();
                }else{
                    ToastEUtil.makeText(mView.getActivity(), response.getMessage(), ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        });
    }

    @Override
    public void requestSendCode(String phone) {
        Connect.SendMobileCode sendMobileCode = Connect.SendMobileCode.newBuilder()
                .setMobile(phone)
                .setCategory(10)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.V2_SMS_SEND, sendMobileCode, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try{
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.SecurityToken securityToken = Connect.SecurityToken .parseFrom(structData.getPlainData());
                    token = securityToken.getToken();
                    countdownTime();
                    mView.setSendCodeStatus(true);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if(response.getCode() == 2400){
                    ToastEUtil.makeText(mView.getActivity(), R.string.Link_Operation_frequent,ToastEUtil.TOAST_STATUS_FAILE).show();
                }else{
                    ToastEUtil.makeText(mView.getActivity(),R.string.Login_SMS_code_sent_failure,ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        });
    }

    private void countdownTime(){
        ExCountDownTimer exCountDownTimer = new ExCountDownTimer(120 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished, int percent) {
                mView.changeBtnTiming(millisUntilFinished / 1000);
            }

            @Override
            public void onPause() {
            }

            @Override
            public void onFinish() {
                mView.changeBtnFinish();
            }
        };
        exCountDownTimer.start();
    }

}
