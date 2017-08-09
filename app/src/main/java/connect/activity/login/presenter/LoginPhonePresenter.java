package connect.activity.login.presenter;

import android.text.Editable;
import android.text.TextWatcher;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.List;

import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.activity.login.bean.CountryBean;
import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.LoginPhoneContract;
import connect.utils.DialogUtil;
import connect.utils.ProgressUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/4/12 0012.
 */

public class LoginPhonePresenter implements LoginPhoneContract.Presenter{

    private final CountryBean countryBean;
    private LoginPhoneContract.View mView;

    public LoginPhonePresenter(LoginPhoneContract.View mView,CountryBean countryBean) {
        this.mView = mView;
        this.countryBean = countryBean;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            try {
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber swissNumberProto = phoneUtil.parse(s.toString(), countryBean.getCountryCode());
                if(phoneUtil.isValidNumberForRegion(swissNumberProto, countryBean.getCountryCode())){
                    mView.setBtnEnabled(true);
                }else{
                    mView.setBtnEnabled(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                mView.setBtnEnabled(false);
            }
        }
    };

    @Override
    public TextWatcher getPhoneTextWatcher() {
        return textWatcher;
    }

    @Override
    public void request(String mobile){
        ProgressUtil.getInstance().showProgress(mView.getActivity());
        Connect.SendMobileCode sendMobileCode = Connect.SendMobileCode.newBuilder()
                .setMobile(mobile)
                .setCategory(1).build();
        HttpRequest.getInstance().post(UriUtil.CONNECT_V1_SMS_SEND, sendMobileCode, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    ProgressUtil.getInstance().dismissProgress();
                    mView.verifySuccess();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                try {
                    ProgressUtil.getInstance().dismissProgress();
                    if(response.getCode() == 2400){
                        ToastEUtil.makeText(mView.getActivity(), R.string.Link_Operation_frequent,ToastEUtil.TOAST_STATUS_FAILE).show();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void showMore(){
        ArrayList<String> list = new ArrayList<>();
        list.add(mView.getActivity().getString(R.string.Login_Scan_your_backup_for_login));
        list.add(mView.getActivity().getString(R.string.Login_Sign_In_Up_Local_account));
        DialogUtil.showBottomView(mView.getActivity(), list, new DialogUtil.DialogListItemClickListener() {
            @Override
            public void confirm(int position) {
                switch (position) {
                    case 0://Scan the backup log in
                        mView.scanPermission();
                        break;
                    case 1://Local account password to log in
                        List<UserBean> list = SharedPreferenceUtil.getInstance().getUserList();
                        if(list == null || list.size() == 0){
                            mView.goinRandomSend();
                        }else{
                            mView.goinLocalLogin();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

}
