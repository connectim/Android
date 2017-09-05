package connect.activity.login.presenter;

import android.text.TextUtils;
import android.util.Base64;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.ScanLoginContract;
import connect.activity.set.presenter.SafetyBackupPresenter;
import connect.ui.activity.R;
import connect.utils.ConfigUtil;
import connect.utils.ProgressUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

public class ScanLoginPresenter implements ScanLoginContract.Presenter {

    private final ScanLoginContract.View mView;

    public ScanLoginPresenter(ScanLoginContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {}

    /**
     * Qr code string.
     *
     * @param value
     */
    @Override
    public void checkString(String value) {
        if (SupportKeyUril.checkPriKey(value)) {
            requestExisted(value);
            return;
        }

        ProgressUtil.getInstance().dismissProgress();
        try {
            String enStr = value.replace(SafetyBackupPresenter.scanHead,"");
            if (TextUtils.isEmpty(enStr)) {
                ToastUtil.getInstance().showToast(R.string.Login_scan_string_error);
            } else {
                byte[] byteArrayDe = Base64.decode(enStr, Base64.DEFAULT);
                Connect.ExoprtPrivkeyQrcode privkeyQrcode = Connect.ExoprtPrivkeyQrcode.parseFrom(byteArrayDe);
                switch (privkeyQrcode.getVersion()) {
                    case 1:
                    case 2:
                        UserBean userBean = new UserBean();
                        userBean.setName(privkeyQrcode.getUsername());
                        userBean.setTalkKey(privkeyQrcode.getEncriptionPri());
                        userBean.setPassHint(privkeyQrcode.getPasswordHint());
                        userBean.setPhone(privkeyQrcode.getPhone());
                        userBean.setAvatar(ConfigUtil.getInstance().serverAddress() + "/avatar/v1/"
                                + privkeyQrcode.getAvatar() + ".jpg");
                        userBean.setConnectId(privkeyQrcode.getConnectId());
                        mView.goIntoCodeLogin(userBean, "");
                        break;
                    default:
                        ToastEUtil.makeText(mView.getActivity(), R.string.Login_Invalid_version_number, ToastEUtil.TOAST_STATUS_FAILE).show();
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastEUtil.makeText(mView.getActivity(),R.string.Login_scan_string_error,ToastEUtil.TOAST_STATUS_FAILE).show();
        }
    }

    /**
     * Query the private key is registered.
     *
     * @param priKey private key
     */
    private void requestExisted(final String priKey) {
        OkHttpUtil.getInstance().postEncry(UriUtil.CONNECT_V1_PRIVATE_EXISTED,
                ByteString.copyFrom(SupportKeyUril.createBinaryRandom()),
                EncryptionUtil.ExtendedECDH.EMPTY,
                priKey,
                SupportKeyUril.getPubKeyFromPriKey(priKey),
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        ProgressUtil.getInstance().dismissProgress();
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY,
                                    priKey,imResponse.getCipherData());
                            if (structData == null) {
                                ToastEUtil.makeText(mView.getActivity(),R.string.Network_equest_failed_please_try_again_later,
                                        ToastEUtil.TOAST_STATUS_FAILE).show();
                                return;
                            }
                            Connect.UserExistedToken existedToken = Connect.UserExistedToken.parseFrom(structData.getPlainData());
                            if (ProtoBufUtil.getInstance().checkProtoBuf(existedToken)) {
                                Connect.UserInfo userInfo = existedToken.getUserInfo();
                                if (ProtoBufUtil.getInstance().checkProtoBuf(userInfo)) {
                                    UserBean userBean = new UserBean();
                                    userBean.setAddress(userInfo.getAddress());
                                    userBean.setName(userInfo.getUsername());
                                    userBean.setAvatar(userInfo.getAvatar());
                                    userBean.setPriKey(priKey);
                                    userBean.setPubKey(userInfo.getPubKey());
                                    userBean.setConnectId(userInfo.getConnectId());
                                    mView.goIntoCodeLogin(userBean,existedToken.getToken());
                                }
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {
                        ProgressUtil.getInstance().dismissProgress();
                        if (response.getCode() == 2404) {
                            // The private key is not registered
                            mView.goIntoRegister(priKey);
                        }
                    }
                });
    }

}
