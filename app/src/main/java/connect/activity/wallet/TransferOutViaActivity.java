package connect.activity.wallet;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.wallet.bean.CurrencyEnum;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.set.SafetyPayFeeActivity;
import connect.activity.wallet.bean.SendOutBean;
import connect.activity.wallet.bean.TransferBean;
import connect.activity.wallet.manager.TransferManager;
import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.ActivityUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.SupportKeyUril;
import instant.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.activity.wallet.view.TransferEditView;
import com.wallet.inter.WalletListener;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * Transfer to outer APP
 */
public class TransferOutViaActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.transfer_edit_view)
    TransferEditView transferEditView;
    @Bind(R.id.ok_btn)
    Button okBtn;

    private TransferOutViaActivity mActivity;
    private TransferManager transferManager;

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, TransferOutViaActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_transfer_outvia);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Wallet_Transfer);
        toolbarTop.setRightText(R.string.Chat_History);
        transferEditView.setEditListener(onEditListener);

        transferManager = new TransferManager(mActivity, CurrencyEnum.BTC);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void goHistory(View view) {
        ActivityUtil.next(mActivity, TransferOutViaHistoryActivity.class);
    }

    @OnClick(R.id.ok_btn)
    void goTransferOut(View view) {
        transferManager.outerTransfer(null, transferEditView.getCurrentBtcLong(), new WalletListener<String>() {
            @Override
            public void success(String value) {
                ParamManager.getInstance().putLatelyTransfer(new TransferBean(2,"","",""));
                requestTransferDetail(value);
            }

            @Override
            public void fail(WalletError error) {
                ToastEUtil.makeText(mActivity,R.string.Login_Send_failed).show();
            }
        });
    }

    private TransferEditView.OnEditListener onEditListener = new TransferEditView.OnEditListener() {
        @Override
        public void onEdit(String value) {
            if (TextUtils.isEmpty(value) ||
                    Double.valueOf(transferEditView.getCurrentBtc()) < 0.0001) {
                okBtn.setEnabled(false);
            } else {
                okBtn.setEnabled(true);
            }
        }

        @Override
        public void setFee() {
            SafetyPayFeeActivity.startActivity(mActivity);
        }
    };

    private void requestTransferDetail(String hashId){
        Connect.BillHashId billHashId = Connect.BillHashId.newBuilder().setHash(hashId).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.TRANSFER_OUTER, billHashId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    final Connect.ExternalBillingInfo billingInfo = Connect.ExternalBillingInfo.parseFrom(structData.getPlainData().toByteArray());
                    if(!ProtoBufUtil.getInstance().checkProtoBuf(billingInfo)){
                        return;
                    }
                    SendOutBean sendOutBean = new SendOutBean();
                    sendOutBean.setType(PacketSendActivity.OUT_VIA);
                    sendOutBean.setUrl(billingInfo.getUrl());
                    sendOutBean.setDeadline(billingInfo.getDeadline());
                    sendOutBean.setHashId(billingInfo.getHash());
                    sendOutBean.setAmount(billingInfo.getAmount());
                    PacketSendActivity.startActivity(mActivity,sendOutBean);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {}
        });
    }

}
