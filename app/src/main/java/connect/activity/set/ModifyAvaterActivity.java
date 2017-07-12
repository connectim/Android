package connect.activity.set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.database.MemoryDataManager;
import connect.ui.activity.R;
import connect.activity.set.contract.ModifyAvaterContract;
import connect.activity.set.presenter.ModifyAvaterPresenter;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ToastEUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.HightEqWidthImage;
import connect.widget.TopToolBar;
import connect.widget.album.entity.ImageInfo;
import connect.widget.album.ui.activity.PhotoAlbumActivity;
import connect.widget.clip.ClipImageActivity;
import connect.widget.takepicture.TakePictureActivity;

/**
 * modify avatar
 * Created by Administrator on 2016/12/5.
 */
public class ModifyAvaterActivity extends BaseActivity implements ModifyAvaterContract.View{

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.avatar_img)
    HightEqWidthImage avatarImg;

    private ModifyAvaterActivity mActivity;
    private ModifyAvaterContract.Presenter presenter;

    public static void startActivity(Activity activity) {
        Bundle bundle = new Bundle();
        ActivityUtil.next(activity, ModifyAvaterActivity.class, bundle);
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
        setPresenter(new ModifyAvaterPresenter(this));
        GlideUtil.loadAvater(avatarImg,  MemoryDataManager.getInstance().getAvatar() + "?size=400");
    }

    @Override
    public void setPresenter(ModifyAvaterContract.Presenter presenter) {
        this.presenter = presenter;
        presenter.start();
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void otherLoginClick(View view) {
        ArrayList<String> list = new ArrayList<>();
        list.add(mActivity.getResources().getString(R.string.Login_Take_Photo));
        list.add(mActivity.getResources().getString(R.string.Login_Select_form_album));
        list.add(mActivity.getResources().getString(R.string.Set_Save_Photo));
        DialogUtil.showBottomListView(mActivity, list, new DialogUtil.DialogListItemClickListener() {
            @Override
            public void confirm(AdapterView<?> parent, View view, int position) {
                switch (position) {
                    case 0://take photo
                        TakePictureActivity.startActivity(mActivity);
                        break;
                    case 1://album
                        PhotoAlbumActivity.startActivity(mActivity,PhotoAlbumActivity.OPEN_ALBUM_CODE,1);
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
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void requestAvaFninish(String path) {
        ToastEUtil.makeText(mActivity,R.string.Set_Set_Avatar_success).show();
        GlideUtil.loadAvater(avatarImg,path + "?size=400");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == PhotoAlbumActivity.OPEN_ALBUM_CODE && requestCode == PhotoAlbumActivity.OPEN_ALBUM_CODE){
            List<ImageInfo> strings = (List<ImageInfo>) data.getSerializableExtra("list");
            if (strings != null && strings.size() > 0) {
                ClipImageActivity.startActivity(mActivity,strings.get(0).getImageFile().getAbsolutePath(),ClipImageActivity.REQUEST_CODE);
            }
        }

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TakePictureActivity.REQUEST_CODE:
                case ClipImageActivity.REQUEST_CODE:
                    String photo_path = data.getExtras().getString("path");
                    presenter.requestAvater(photo_path);
                    break;
                default:
                    break;
            }
        }
    }

}
