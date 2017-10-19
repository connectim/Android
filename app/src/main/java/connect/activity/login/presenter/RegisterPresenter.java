package connect.activity.login.presenter;

import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.File;

import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.RegisterContract;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.BitmapUtil;
import connect.utils.FileUtil;
import connect.utils.ProgressUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import instant.utils.cryption.EncryptionUtil;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

public class RegisterPresenter implements RegisterContract.Presenter {

    private RegisterContract.View mView;
    private String passwordHint = "";
    private String headPath = "https://short.connect.im/avatar/v1/b040e0a970bc6d80b675586c5a55f9e9109168ba.png";
    private String talkKey;

    public RegisterPresenter(RegisterContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {}

    /*@Override
    public void setPasswordHintData(String passwordHint) {
        this.passwordHint = passwordHint;
    }*/

    /**
     * Upload the picture.
     *
     * @param pathLocal avatar path
     */
    @Override
    public void requestUserHead(final String pathLocal) {
        ProgressUtil.getInstance().showProgress(mView.getActivity());
        File file = BitmapUtil.getInstance().compress(pathLocal);
        String path = file.getAbsolutePath();
        byte[] headByte = BitmapUtil.bmpToByteArray(BitmapFactory.decodeFile(path),100);
        FileUtil.deleteFile(path);
        HttpRequest.getInstance().post(UriUtil.AVATAR_V1_UP, headByte, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                ProgressUtil.getInstance().dismissProgress();
                // Delete the cropping images
                if (!TextUtils.isEmpty(pathLocal)) {
                    FileUtil.deleteFile(pathLocal);
                }
                // After successful upload display image
                try {
                    Connect.AvatarInfo userAvatar = Connect.AvatarInfo.parseFrom(response.getBody());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(userAvatar)) {
                        headPath = userAvatar.getUrl();
                        mView.showAvatar(headPath);
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                if (!TextUtils.isEmpty(pathLocal)) {
                    FileUtil.deleteFile(pathLocal);
                }
                ProgressUtil.getInstance().dismissProgress();
                ToastEUtil.makeText(mView.getActivity(),R.string.Login_Avatar_upload_failed,ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        });
    }

    /**
     * Registered users.
     *
     * @param nicName
     * @param token Check the phone number of the token
     * @param userBean
     */
    @Override
    public void registerUser(final String nicName, final String token, final UserBean userBean) {
        Connect.RegisterUser.Builder builder = Connect.RegisterUser.newBuilder();
        builder.setUsername(nicName);
        builder.setToken(token);
        builder.setMobile(userBean.getPhone());
        builder.setAvatar(headPath);

        OkHttpUtil.getInstance().postEncry(UriUtil.CONNECT_V1_SIGN_UP, builder.build(),
                EncryptionUtil.ExtendedECDH.EMPTY,
                userBean.getPriKey(),
                userBean.getPubKey(),
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        ProgressUtil.getInstance().dismissProgress();
                        userBean.setAvatar(headPath);
                        userBean.setName(nicName);
                        SharedPreferenceUtil.getInstance().loginSaveUserBean(userBean, mView.getActivity());
                        mView.complete(userBean.isBack());
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {
                        if (response.getCode() == 2101) {
                            Toast.makeText(mView.getActivity(), R.string.Login_User_avatar_is_illegal, Toast.LENGTH_LONG).show();
                        } else if(response.getCode() == 2102){
                            Toast.makeText(mView.getActivity(), R.string.Login_username_already_exists, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(mView.getActivity(), response.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        ProgressUtil.getInstance().dismissProgress();
                    }
                });

        /*new AsyncTask<Void,Void,Connect.RegisterUser>() {
            @Override
            protected Connect.RegisterUser doInBackground(Void... params) {
                Connect.RegisterUser.Builder builder = Connect.RegisterUser.newBuilder();
                builder.setUsername(nicName);
                // builder.setPasswordHint(passwordHint);
                // Determine whether the binding number
                *//*if (!TextUtils.isEmpty(token)) {
                    builder.setToken(token);
                    builder.setMobile(userBean.getPhone());
                }*//*
                builder.setToken(token);
                builder.setMobile(userBean.getPhone());
                builder.setAvatar(headPath);
                // Password encryption private key
                *//*talkKey = SupportKeyUril.createTalkKey(userBean.getPriKey(),userBean.getAddress(),password);
                builder.setEncryptionPri(talkKey);*//*
                Connect.RegisterUser registerUser = builder.build();
                return registerUser;
            }

            @Override
            protected void onPostExecute(Connect.RegisterUser registerUser) {
                super.onPostExecute(registerUser);
                userBean.setName(nicName);
                requestRegister(registerUser,userBean);
            }
        }.execute();*/
    }

    /*private void requestRegister(Connect.RegisterUser registerUser, final UserBean userBean) {
        OkHttpUtil.getInstance().postEncry(UriUtil.CONNECT_V1_SIGN_UP,
                registerUser,
                EncryptionUtil.ExtendedECDH.EMPTY,
                userBean.getPriKey(),
                userBean.getPubKey(),
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        ProgressUtil.getInstance().dismissProgress();
                        //userBean.setTalkKey(talkKey);
                        userBean.setAvatar(headPath);
                        userBean.setPassHint(passwordHint);
                        SharedPreferenceUtil.getInstance().loginSaveUserBean(userBean, mView.getActivity());
                        mView.complete(userBean.isBack());
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {
                        if (response.getCode() == 2101) {
                            Toast.makeText(mView.getActivity(), R.string.Login_User_avatar_is_illegal, Toast.LENGTH_LONG).show();
                        } else if(response.getCode() == 2102){
                            Toast.makeText(mView.getActivity(), R.string.Login_username_already_exists, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(mView.getActivity(), response.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        ProgressUtil.getInstance().dismissProgress();
                    }
                });
    }*/


}
