package connect.ui.activity.set;

import android.app.Activity;
import android.app.Dialog;
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
import connect.ui.base.BaseActivity;
import connect.ui.service.UpdataService;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.StringUtil;
import connect.utils.permission.PermissiomUtilNew;
import connect.utils.system.SystemDataUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
import protos.Connect;

/**
 * about
 * Created by john on 2016/11/30.
 */

public class AboutActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.appVersion)
    TextView appVersion;
    @Bind(R.id.llRate)
    LinearLayout llRate;
    @Bind(R.id.llOpenSource)
    LinearLayout llOpenSource;
    @Bind(R.id.tvNewVersion)
    TextView tvNewVersion;
    @Bind(R.id.llUpdate)
    LinearLayout llUpdate;

    private AboutActivity mActivity;
    private Connect.VersionResponse versionResponse;
    private int comparInt = 0;
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
        requestAppUpdata();
    }

    @OnClick(R.id.left_img)
    void goback(View view){
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.llOpenSource)
    void goOpenSource(View view){
        ActivityUtil.next(mActivity,DeveloperActivity.class);
    }

    @OnClick(R.id.llUpdate)
    void goUpdate(View view){
        if(comparInt == 1){
            Dialog dialogUpdata = DialogUtil.showAlertTextView(mActivity,
                    getString(R.string.Set_Found_new_version), versionResponse.getRemark(),
                    "", getString(R.string.Set_Now_update_app),
                    false, new DialogUtil.OnItemClickListener() {
                        @Override
                        public void confirm(String value) {
                            if(!TextUtils.isEmpty(versionResponse.getUpgradeUrl())){
                                downLoadPath = versionResponse.getUpgradeUrl();
                                PermissiomUtilNew.getInstance().requestPermissom(mActivity,new String[]{PermissiomUtilNew.PERMISSIM_STORAGE},permissomCallBack);
                            }
                        }

                        @Override
                        public void cancel() {

                        }
                    },false);
        }
    }

    private PermissiomUtilNew.ResultCallBack permissomCallBack = new PermissiomUtilNew.ResultCallBack(){
                @Override
                public void granted(String[] permissions) {
                    Intent intent = new Intent(mActivity, UpdataService.class);
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
        PermissiomUtilNew.getInstance().onRequestPermissionsResult
                (mActivity,requestCode,permissions,grantResults,permissomCallBack);
    }

    private void requestAppUpdata(){
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
                    if(ProtoBufUtil.getInstance().checkProtoBuf(versionResponse)){
                        comparInt = StringUtil.VersionComparison(versionResponse.getVersion(),SystemDataUtil.getVersionName(mActivity));
                        switch (comparInt){
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
            public void onError(Connect.HttpNotSignResponse response) {

            }
        });
    }

}
