package connect.ui.activity.home.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import connect.ui.activity.R;
import connect.ui.service.UpdataService;
import connect.utils.DialogUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.StringUtil;
import connect.utils.permission.PermissionUtil;
import connect.utils.system.SystemDataUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/3/30 0030.
 */

public class CheckUpdata {

    private Activity activity;
    private String downLoadpath;

    public CheckUpdata(Activity activity) {
        this.activity = activity;
    }

    public void check(){
        Connect.VersionRequest versionRequest = Connect.VersionRequest.newBuilder()
                .setCategory(1)
                .setPlatform(2)
                .setProtocolVersion(0)
                .setVersion(SystemDataUtil.getVersionName(activity))
                .build();
        HttpRequest.getInstance().post(UriUtil.CONNECT_V1_VERSION, versionRequest, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody().toByteArray());
                    Connect.VersionResponse versionResponse = Connect.VersionResponse.parseFrom(structData.getPlainData());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(versionResponse)
                            && StringUtil.VersionComparison(versionResponse.getVersion(), SystemDataUtil.getVersionName(activity)) == 1) {
                        showUpdataDialog(versionResponse);
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

    private void showUpdataDialog(final Connect.VersionResponse versionResponse) {
        boolean isCancle = versionResponse.getForce();
        Dialog dialogUpdata = DialogUtil.showAlertTextView(activity,
                activity.getResources().getString(R.string.Set_Found_new_version), versionResponse.getRemark(),
                "", activity.getResources().getString(R.string.Set_Now_update_app),
                isCancle, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        if (!TextUtils.isEmpty(versionResponse.getUpgradeUrl())) {
                            downLoadpath = versionResponse.getUpgradeUrl();
                            PermissionUtil.getInstance().requestPermissom(activity, new String[]{PermissionUtil.PERMISSIM_STORAGE}, permissomCallBack);
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                }, false);

        if (isCancle) {
            dialogUpdata.setCancelable(false);
        }

    }

    public PermissionUtil.ResultCallBack permissomCallBack = new PermissionUtil.ResultCallBack(){
        @Override
        public void granted(String[] permissions) {
            Intent intent = new Intent(activity, UpdataService.class);
            intent.putExtra("downLoadUrl", downLoadpath);
            activity.startService(intent);
        }

        @Override
        public void deny(String[] permissions) {
        }
    };

}
