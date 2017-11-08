package connect.activity.chat.set;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.chat.set.contract.GroupQRContract;
import connect.activity.chat.set.presenter.GroupQRPresenter;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.BitmapUtil;
import connect.utils.DialogUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;
import connect.widget.zxing.utils.CreateScan;

/**
 * group QRcode Information
 */
public class GroupQRActivity extends BaseActivity implements GroupQRContract.BView{

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.roundimg1)
    ImageView roundimg1;
    @Bind(R.id.txt1)
    TextView txt1;
    @Bind(R.id.img1)
    ImageView img1;
    @Bind(R.id.txt3)
    TextView txt3;

    private GroupQRActivity activity;
    private static String TAG = "_GroupQRActivity";
    private static String GROUP_IDENTIFY = "GROUP_IDENTIFY";
    private String groupKey = null;
    private GroupQRContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_qr);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString(GROUP_IDENTIFY, groupkey);
        ActivityUtil.next(activity, GroupQRActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Link_Group_is_QR_Code));
        toolbar.setRightImg(R.mipmap.menu_white);
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });
        toolbar.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupMemberEntity myMember = ContactHelper.getInstance().loadGroupMemberEntity(groupKey,
                        SharedPreferenceUtil.getInstance().getUser().getUid());
                final ArrayList<String> list = new ArrayList<>();
                if (myMember.getRole() == 1) {
                    list.add(activity.getResources().getString(R.string.Link_Refresh_QR_Code));
                }
                list.add(getString(R.string.Link_Share));
                list.add(getString(R.string.Set_Save_Photo));
                DialogUtil.showBottomView(activity, list, new DialogUtil.DialogListItemClickListener() {
                    @Override
                    public void confirm(int position) {
                        if (activity.getResources().getString(R.string.Link_Refresh_QR_Code).equals(list.get(position))) {//refresh qrcode
                            DialogUtil.showAlertTextView(activity, "", getString(R.string.Link_Refresh_QR_tip), getString(R.string.Common_Cancel), getString(R.string.Common_OK),
                                    false, new DialogUtil.OnItemClickListener() {
                                        @Override
                                        public void confirm(String value) {
                                            presenter.requestGroupQR(false);
                                        }

                                        @Override
                                        public void cancel() {

                                        }
                                    });
                        } else if (activity.getResources().getString(R.string.Set_Save_Photo).equals(list.get(position))) {
                            BitmapUtil.getInstance().bitmapSavePath(img1.getDrawingCache());
                        } else if (getString(R.string.Link_Share).equals(list.get(position))) {//share group address
                            presenter.requestGroupShare();
                        }
                    }
                });
            }
        });

        groupKey = getIntent().getStringExtra(GROUP_IDENTIFY);
        new GroupQRPresenter(this).start();
        presenter.requestGroupQR(true);
    }

    @Override
    public String getRoomKey() {
        return groupKey;
    }


    @Override
    public void setPresenter(GroupQRContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public void groupAvatar(String avatar) {
        GlideUtil.loadAvatarRound(roundimg1, avatar);
    }

    @Override
    public void groupName(String groupname) {
        txt1.setText(groupname);
    }

    @Override
    public void groupHash(String hash) {
        if (TextUtils.isEmpty(hash)) {
            toolbar.setRightImg(null);
            txt3.setVisibility(View.VISIBLE);
        } else {
            txt3.setVisibility(View.GONE);
            CreateScan createScan = new CreateScan();
            Bitmap bitmap = createScan.generateQRCode(hash, getResources().getColor(R.color.color_white));
            img1.setImageBitmap(bitmap);
        }
    }
}
