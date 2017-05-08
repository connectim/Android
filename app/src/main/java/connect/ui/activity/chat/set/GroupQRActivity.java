package connect.ui.activity.chat.set;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.GroupEntity;
import connect.db.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.BitmapUtil;
import connect.utils.DialogUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
import connect.view.roundedimageview.RoundedImageView;
import connect.view.zxing.utils.CreateScan;
import protos.Connect;

/**
 * group QRcode Information
 */
public class GroupQRActivity extends BaseActivity {

    private static String GROUP_KEY = "GROUP_KEY";
    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.roundimg1)
    RoundedImageView roundimg1;
    @Bind(R.id.txt1)
    TextView txt1;
    @Bind(R.id.img1)
    ImageView img1;
    @Bind(R.id.txt3)
    TextView txt3;

    private GroupQRActivity activity;
    private String groupKey = null;
    private GroupEntity groupEntity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_qr);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString(GROUP_KEY, groupkey);
        ActivityUtil.next(activity, GroupQRActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Link_Group_is_QR_Code));
        toolbar.setRightImg(R.mipmap.menu_white);
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });
        toolbar.setRightListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupMemberEntity myMember = ContactHelper.getInstance().loadGroupMemByAds(groupKey, SharedPreferenceUtil.getInstance().getAddress());
                final ArrayList<String> list = new ArrayList<>();
                if (myMember.getRole() == 1) {
                    list.add(activity.getResources().getString(R.string.Link_Refresh_QR_Code));
                }
                list.add(getString(R.string.Link_Share));
                list.add(getString(R.string.Set_Save_Photo));
                DialogUtil.showBottomListView(activity, list, new DialogUtil.DialogListItemClickListener() {
                    @Override
                    public void confirm(AdapterView<?> parent, View view, int position) {
                        if (activity.getResources().getString(R.string.Link_Refresh_QR_Code).equals(list.get(position))) {//refresh qrcode
                            DialogUtil.showAlertTextView(activity, "", getString(R.string.Link_Refresh_QR_tip), getString(R.string.Common_Cancel), getString(R.string.Common_OK),
                                    false, new DialogUtil.OnItemClickListener() {
                                        @Override
                                        public void confirm(String value) {
                                            groupQRInfo(UriUtil.GROUP_REFRESH_HASH, groupEntity.getIdentifier());
                                        }

                                        @Override
                                        public void cancel() {

                                        }
                                    });
                        } else if (activity.getResources().getString(R.string.Link_Refresh_QR_Code).equals(list.get(position))) {
                            BitmapUtil.bitmapSavePath(img1.getDrawingCache());
                        } else if (getString(R.string.Link_Share).equals(list.get(position))) {//share group address
                            shareGroupUrl();
                        }
                    }
                });
            }
        });

        groupKey = getIntent().getStringExtra(GROUP_KEY);
        groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
        if(groupEntity==null){
            ActivityUtil.goBack(activity);
            return;
        }
        GlideUtil.loadAvater(roundimg1, groupEntity.getAvatar());
        txt1.setText(groupEntity.getName());

        groupQRInfo(UriUtil.GROUP_HASH, groupEntity.getIdentifier());
    }

    /**
     * group qrcode
     */
    protected void groupQRInfo(final String url, String value) {
        Connect.GroupId groupId = Connect.GroupId.newBuilder()
                .setIdentifier(value).build();
        OkHttpUtil.getInstance().postEncrySelf(url, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                String prikey = SharedPreferenceUtil.getInstance().getPriKey();
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(prikey, imResponse.getCipherData());
                    Connect.GroupHash groupHash = Connect.GroupHash.parseFrom(structData.getPlainData());

                    String hash = groupHash.getHash();
                    if (TextUtils.isEmpty(hash)) {
                        toolbar.setRightImg(null);
                        txt3.setVisibility(View.VISIBLE);
                    } else {
                        txt3.setVisibility(View.GONE);
                        CreateScan createScan = new CreateScan();
                        Bitmap bitmap = createScan.generateQRCode(hash, getResources().getColor(R.color.color_white));
                        img1.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if(url.equals(UriUtil.GROUP_REFRESH_HASH)){
                    ToastEUtil.makeText(activity,R.string.Link_Refresh_QR_code_failed,ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        });
    }

    protected void shareGroupUrl(){
        Connect.GroupId groupId= Connect.GroupId.newBuilder()
                .setIdentifier(groupKey).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_GROUP_SHARE, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                String prikey = SharedPreferenceUtil.getInstance().getPriKey();
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(prikey, imResponse.getCipherData());
                    Connect.GroupUrl groupUrl = Connect.GroupUrl.parseFrom(structData.getPlainData());
                    if(TextUtils.isEmpty(groupUrl.getUrl())){
                        ToastEUtil.makeText(activity,R.string.Link_Share_failed,ToastEUtil.TOAST_STATUS_FAILE).show();
                        return;
                    }
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, groupUrl.getUrl());
                    shareIntent.setType("text/plain");
                    startActivity(Intent.createChooser(shareIntent, "share to"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(activity,R.string.Link_The_group_is_not_public_Not_Share,ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        });
    }
}
