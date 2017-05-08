package connect.ui.activity.login.presenter;

import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.ui.activity.login.bean.CountryBean;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.login.contract.LoginPhoneContract;
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
    }

    @Override
    public void start() {

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
        DialogUtil.showBottomListView(mView.getActivity(), list, new DialogUtil.DialogListItemClickListener() {
            @Override
            public void confirm(AdapterView<?> parent, View view, int position) {
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
