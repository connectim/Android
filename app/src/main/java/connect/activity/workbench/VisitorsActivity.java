package connect.activity.workbench;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseFragmentActivity;
import connect.activity.workbench.fragment.ApprovedFragment;
import connect.activity.workbench.fragment.AuditFragment;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.BitmapUtil;
import connect.service.UpdateAppService;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.FileUtil;
import connect.utils.ProgressUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.utils.permission.PermissionUtil;
import connect.widget.TopToolBar;
import connect.widget.zxing.utils.CreateScan;
import protos.Connect;

public class VisitorsActivity extends BaseFragmentActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.to_audit_text)
    TextView toAuditText;
    @Bind(R.id.to_audit_line)
    View toAuditLine;
    @Bind(R.id.the_approved_text)
    TextView theApprovedText;
    @Bind(R.id.the_approved_line)
    View theApprovedLine;
    @Bind(R.id.content_fragment)
    FrameLayout contentFragment;

    private VisitorsActivity mActivity;

    private AuditFragment auditFragment;
    private ApprovedFragment approvedFragment;

    public static void lunchActivity(Activity activity) {
        ActivityUtil.next(activity, VisitorsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workbench_visitors);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Work_Visitors_info);
        toolbarTop.setRightText(R.string.Link_Invite);

        auditFragment = AuditFragment.startFragment();
        approvedFragment = ApprovedFragment.startFragment();

        switchFragment(0);
        toAuditText.setSelected(true);
        toAuditLine.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick({R.id.to_audit_text, R.id.the_approved_text})
    public void OnClickListener(View view) {
        toAuditText.setSelected(false);
        toAuditLine.setVisibility(View.GONE);
        theApprovedText.setSelected(false);
        theApprovedLine.setVisibility(View.GONE);

        switch (view.getId()) {
            case R.id.to_audit_text:
                switchFragment(0);
                toAuditText.setSelected(true);
                toAuditLine.setVisibility(View.VISIBLE);
                break;
            case R.id.the_approved_text:
                switchFragment(1);
                theApprovedText.setSelected(true);
                theApprovedLine.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void switchFragment(int code) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment.isVisible()) {
                    fragmentTransaction.hide(fragment);
                }
            }
        }
        switch (code) {
            case 0:
                if (!auditFragment.isAdded()) {
                    fragmentTransaction.add(R.id.content_fragment, auditFragment);
                } else {
                    fragmentTransaction.show(auditFragment);
                }
                break;
            case 1:
                if (!approvedFragment.isAdded()) {
                    fragmentTransaction.add(R.id.content_fragment, approvedFragment);
                } else {
                    fragmentTransaction.show(approvedFragment);
                }
                break;
        }

        //commit :IllegalStateException: Can not perform this action after onSaveInstanceState
        fragmentTransaction.commitAllowingStateLoss();
    }

    private Connect.Staff staff1;
    @OnClick(R.id.right_lin)
    void shareInvite(View view) {
        ProgressUtil.getInstance().showProgress(mActivity);
        Connect.Staff staff = Connect.Staff.newBuilder().build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V3_PROXY_TOKEN, staff, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                ProgressUtil.getInstance().dismissProgress();
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    staff1 = Connect.Staff.parseFrom(structData.getPlainData());

                    PermissionUtil.getInstance().requestPermission(mActivity,
                            new String[]{PermissionUtil.PERMISSION_STORAGE},
                            permissionCallBack);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                ToastUtil.getInstance().showToast(response.getMessage());
                ProgressUtil.getInstance().dismissProgress();
            }
        });
    }


    private File saveBitmap(Bitmap bm) {
        String path = FileUtil.newSdcardTempFile(FileUtil.FileType.IMG).getAbsolutePath();
        File f = new File(path);
        try {
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }

            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            return f;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void shareMsg(String activityTitle, String msgTitle, String msgText, File file) {
        try {
            String filepath = file.getAbsolutePath();
            String imageUri = MediaStore.Images.Media.insertImage(mActivity.getContentResolver(), filepath, msgTitle, msgText);

            Intent intent = new Intent(Intent.ACTION_SEND);
            if (file == null) {
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
                intent.putExtra(Intent.EXTRA_TEXT, msgText);
            } else {
                if (file != null && file.exists() && file.isFile()) {
                    intent.setType("image/jpg");
                    Uri u = Uri.fromFile(file);
                    intent.putExtra(Intent.EXTRA_STREAM, u);
                }
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, activityTitle));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.getInstance().onRequestPermissionsResult(mActivity, requestCode, permissions, grantResults, permissionCallBack);
    }

    private PermissionUtil.ResultCallBack permissionCallBack = new PermissionUtil.ResultCallBack(){
        @Override
        public void granted(String[] permissions) {
            CreateScan createScan = new CreateScan();
            Bitmap bitmap = createScan.generateQRCode("https://wx-kq.bitmain.com/guest/info?token=" + staff1.getToken());
            File file = saveBitmap(bitmap);
            shareMsg(getResources().getString(R.string.Work_Visitors_share), "", "", file);
        }

        @Override
        public void deny(String[] permissions) {
        }
    };

    /*public void shareMsg(String activityTitle, String msgTitle, String msgText, File file) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (file == null) {
            intent.setType("text/plain");
        } else {
            if (file != null && file.exists() && file.isFile()) {
                intent.setType("image/jpg");
                Uri u = Uri.fromFile(file);
                intent.putExtra(Intent.EXTRA_STREAM, u);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, activityTitle));
    }*/

}
