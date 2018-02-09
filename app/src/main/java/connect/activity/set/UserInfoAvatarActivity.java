package connect.activity.set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.activity.set.contract.UserInfoAvatarContract;
import connect.activity.set.presenter.UserInfoAvatarPresenter;
import connect.utils.ActivityUtil;
import connect.utils.dialog.DialogUtil;
import connect.utils.ToastEUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.HeightEqWidthImage;
import connect.widget.TopToolBar;
import connect.widget.album.AlbumActivity;
import connect.widget.album.model.AlbumFile;
import connect.widget.clip.ClipImageActivity;
import connect.widget.takepicture.TakePictureActivity;

/**
 * Modify the user's head.
 */
public class UserInfoAvatarActivity extends BaseActivity implements UserInfoAvatarContract.View {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.avatar_img)
    HeightEqWidthImage avatarImg;

    private UserInfoAvatarActivity mActivity;
    private UserInfoAvatarContract.Presenter presenter;

    public static void startActivity(Activity activity) {
        Bundle bundle = new Bundle();
        ActivityUtil.next(activity, UserInfoAvatarActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_modifyavater);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Chat_Photo);
        toolbarTop.setRightImg(R.mipmap.menu_white);
        setPresenter(new UserInfoAvatarPresenter(this));
        GlideUtil.loadAvatarRound(avatarImg,  SharedPreferenceUtil.getInstance().getUser().getAvatar() + "?size=400", 0);
    }

    @Override
    public void setPresenter(UserInfoAvatarContract.Presenter presenter) {
        this.presenter = presenter;
        presenter.start();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void otherLoginClick(View view) {
        ArrayList<String> list = new ArrayList<>();
        list.add(mActivity.getResources().getString(R.string.Login_Take_Photo));
        list.add(mActivity.getResources().getString(R.string.Login_Select_form_album));
        list.add(mActivity.getResources().getString(R.string.Set_Save_Photo));
        DialogUtil.showBottomView(mActivity, list, new DialogUtil.DialogListItemClickListener() {
            @Override
            public void confirm(int position) {
                switch (position) {
                    case 0://take photo
                        TakePictureActivity.startActivity(mActivity);
                        break;
                    case 1://album
                        AlbumActivity.startActivity(mActivity,AlbumActivity.OPEN_ALBUM_CODE,1);
                        break;
                    case 2://save in phone
                        presenter.saveImageToGallery();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == AlbumActivity.OPEN_ALBUM_CODE && requestCode == AlbumActivity.OPEN_ALBUM_CODE){
            List<AlbumFile> strings = (List<AlbumFile>) data.getSerializableExtra("list");
            if (strings != null && strings.size() > 0) {
                ClipImageActivity.startActivity(mActivity,strings.get(0).getPath(),ClipImageActivity.REQUEST_CODE);
            }
        }

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TakePictureActivity.REQUEST_CODE:
                case ClipImageActivity.REQUEST_CODE:
                    String path = data.getExtras().getString("path");
                    presenter.requestAvatar(path);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void requestAvaFinish(String path) {
        ToastEUtil.makeText(mActivity,R.string.Set_Set_Avatar_success).show();
        GlideUtil.loadAvatarRound(avatarImg, path + "?size=400", 0);
    }

}
