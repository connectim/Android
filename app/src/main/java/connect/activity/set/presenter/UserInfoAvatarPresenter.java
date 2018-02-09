package connect.activity.set.presenter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.File;
import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.GroupMemberUtil;
import connect.activity.login.bean.UserBean;
import connect.activity.set.contract.UserInfoAvatarContract;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.utils.BitmapUtil;
import connect.utils.FileUtil;
import connect.utils.ProgressUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

public class UserInfoAvatarPresenter implements UserInfoAvatarContract.Presenter {

    private UserInfoAvatarContract.View mView;
    private MediaScannerConnection scanner;
    private String pathDcim;

    public UserInfoAvatarPresenter(UserInfoAvatarContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void start() {
        scanner = new MediaScannerConnection(mView.getActivity(), new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {
                if (pathDcim != null) {
                    scanner.scanFile(pathDcim,"media*//*");
                }
            }

            @Override
            public void onScanCompleted(String path, Uri uri) {
                scanner.disconnect();
            }
        });
    }

    @Override
    public void saveImageToGallery() {
        String path = SharedPreferenceUtil.getInstance().getUser().getAvatar();
        if (!TextUtils.isEmpty(path)) {
            Glide.with(BaseApplication.getInstance())
                    .load(path + "?size=500")
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            if (resource != null) {
                                saveNotify(resource);
                            } else {
                                ToastEUtil.makeText(mView.getActivity(),R.string.Set_Save_Failed);
                            }
                        }
                    });
        }
    }

    private void saveNotify(Bitmap bmp) {
        File file = BitmapUtil.getInstance().bitmapSavePathDCIM(bmp);
        pathDcim = file.getAbsolutePath();
        try {
            scanner.connect();
            ToastEUtil.makeText(mView.getActivity(), R.string.Login_Save_successful).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void requestAvatar(final String pathLocal) {
        ProgressUtil.getInstance().showProgress(mView.getActivity());
        new AsyncTask<Void, Void, Connect.Avatar>() {
            @Override
            protected Connect.Avatar doInBackground(Void... params) {
                File file = BitmapUtil.getInstance().compress(pathLocal);
                String path = file.getAbsolutePath();
                byte[] headByte = BitmapUtil.bmpToByteArray(BitmapFactory.decodeFile(path),100);
                Connect.Avatar avatar = Connect.Avatar.newBuilder()
                        .setFile(ByteString.copyFrom(headByte))
                        .build();
                FileUtil.deleteFile(path);
                FileUtil.deleteFile(pathLocal);
                return avatar;
            }

            @Override
            protected void onPostExecute(Connect.Avatar avatar) {
                super.onPostExecute(avatar);
                OkHttpUtil.getInstance().postEncrySelf(UriUtil.AVATAR_V1_SET, avatar, new ResultCall<Connect.HttpNotSignResponse>() {
                    @Override
                    public void onResponse(Connect.HttpNotSignResponse response) {
                        ProgressUtil.getInstance().dismissProgress();
                        try {
                            Connect.StructData structData = Connect.StructData.parseFrom(response.getBody().toByteArray());
                            Connect.AvatarInfo userAvatar = Connect.AvatarInfo.parseFrom(structData.getPlainData());
                            if (ProtoBufUtil.getInstance().checkProtoBuf(userAvatar)) {
                                UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                                userBean.setAvatar(userAvatar.getUrl());
                                SharedPreferenceUtil.getInstance().putUser(userBean);

                                mView.requestAvaFinish(userAvatar.getUrl());


                                List<GroupMemberEntity> memberEntities = ContactHelper.getInstance().loadGroupMembersByUid(userBean.getUid());
                                if (memberEntities != null && memberEntities.size() > 0) {
                                    GroupMemberUtil.getIntance().clearMembersMap();
                                    for (GroupMemberEntity entity : memberEntities) {
                                        entity.setAvatar(userAvatar.getUrl());
                                    }
                                    ContactHelper.getInstance().inserGroupMemEntity(memberEntities);
                                }
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpNotSignResponse response) {
                        ProgressUtil.getInstance().dismissProgress();
                        if(response.getCode() == 2408){
                            ToastEUtil.makeText(mView.getActivity(),R.string.Login_User_avatar_is_illegal,ToastEUtil.TOAST_STATUS_FAILE).show();
                        }else{
                            ToastEUtil.makeText(mView.getActivity(),R.string.Link_update_Failed,ToastEUtil.TOAST_STATUS_FAILE).show();
                        }

                    }
                });
            }
        }.execute();
    }

}
