package connect.activity.set.presenter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.File;
import java.io.FileNotFoundException;

import connect.database.MemoryDataManager;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.activity.login.bean.UserBean;
import connect.activity.set.contract.UserInfoAvatarContract;
import connect.activity.base.BaseApplication;
import connect.utils.BitmapUtil;
import connect.utils.FileUtil;
import connect.utils.ProgressUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
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
        String path = MemoryDataManager.getInstance().getAvatar();
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
    public void requestAvater(final String pathLocal) {
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
                OkHttpUtil.getInstance().postEncrySelf(UriUtil.AVATAR_V1_SET, avatar, new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        ProgressUtil.getInstance().dismissProgress();
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                            Connect.AvatarInfo userAvatar = Connect.AvatarInfo.parseFrom(structData.getPlainData());
                            if (ProtoBufUtil.getInstance().checkProtoBuf(userAvatar)) {
                                UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                                userBean.setAvatar(userAvatar.getUrl());
                                SharedPreferenceUtil.getInstance().putUser(userBean);

                                mView.requestAvaFninish(userAvatar.getUrl());
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {
                        ProgressUtil.getInstance().dismissProgress();
                        ToastEUtil.makeText(mView.getActivity(),R.string.Link_update_Failed,ToastEUtil.TOAST_STATUS_FAILE).show();
                    }
                });
            }
        }.execute();
    }

}
