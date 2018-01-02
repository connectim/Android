package connect.activity.set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ui.activity.R;
import connect.activity.base.BaseActivity;
import connect.service.UpdateAppService;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.StringUtil;
import connect.utils.permission.PermissionUtil;
import connect.utils.system.SystemDataUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * About the App
 */
public class AboutActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.appVersion)
    TextView appVersion;
    @Bind(R.id.llRate)
    LinearLayout llRate;
    @Bind(R.id.tvNewVersion)
    TextView tvNewVersion;
    @Bind(R.id.llUpdate)
    LinearLayout llUpdate;

    private AboutActivity mActivity;
    private Connect.VersionResponse versionResponse;
    private int compareInt = 0;
    private String downLoadPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_about);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, AboutActivity.class);
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_About);
        appVersion.setText(getString(R.string.Set_Versions_number, SystemDataUtil.getVersionName(mActivity)));
        requestAppUpdate();
    }

    @OnClick(R.id.left_img)
    void goback(View view){
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.llUpdate)
    void goUpdate(View view){
        if(compareInt == 1){
            DialogUtil.showAlertTextView(mActivity, getString(R.string.Set_Found_new_version),
                    versionResponse.getRemark(), "", getString(R.string.Set_Now_update_app),
                    false, false, new DialogUtil.OnItemClickListener() {
                        @Override
                        public void confirm(String value) {
                            if(!TextUtils.isEmpty(versionResponse.getUpgradeUrl())){
                                downLoadPath = versionResponse.getUpgradeUrl();
                                PermissionUtil.getInstance().requestPermission(mActivity,new String[]{PermissionUtil.PERMISSION_STORAGE},permissionCallBack);
                            }
                        }

                        @Override
                        public void cancel() {}
                    });
        }
    }

    private PermissionUtil.ResultCallBack permissionCallBack = new PermissionUtil.ResultCallBack(){
                @Override
                public void granted(String[] permissions) {
                    Intent intent = new Intent(mActivity, UpdateAppService.class);
                    intent.putExtra("downLoadUrl", downLoadPath);
                    startService(intent);
                }

                @Override
                public void deny(String[] permissions) {
                }
            };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        PermissionUtil.getInstance().onRequestPermissionsResult
                (mActivity,requestCode,permissions,grantResults,permissionCallBack);
    }

    private void requestAppUpdate(){
        Connect.VersionRequest versionRequest = Connect.VersionRequest.newBuilder()
                .setCategory(1)
                .setPlatform(2)
                .setProtocolVersion(0)
                .setVersion(SystemDataUtil.getVersionName(mActivity))
                .build();
        HttpRequest.getInstance().post(UriUtil.CONNECT_V1_VERSION, versionRequest, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody().toByteArray());
                    versionResponse = Connect.VersionResponse.parseFrom(structData.getPlainData());
                    if(!TextUtils.isEmpty(versionResponse.getVersion())){
                        compareInt = StringUtil.VersionComparison(versionResponse.getVersion(),SystemDataUtil.getVersionName(mActivity));
                        switch (compareInt){
                            case 1:
                                tvNewVersion.setText(mActivity.getString(R.string.Set_new_version,versionResponse.getVersion()));
                                break;
                            case 0:
                                tvNewVersion.setText(R.string.Set_This_is_the_newest_version);
                            case -1:
                                tvNewVersion.setText(R.string.Set_This_is_the_newest_version);
                                break;
                        }
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {}
        });
    }

}
