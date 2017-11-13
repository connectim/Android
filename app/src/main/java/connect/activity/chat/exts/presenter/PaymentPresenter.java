package connect.activity.chat.exts.presenter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;

import com.wallet.bean.CurrencyEnum;

import java.util.List;

import connect.activity.chat.bean.ContainerBean;
import connect.activity.chat.bean.LinkMessageRow;
import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.exts.contract.PaymentContract;
import connect.activity.login.bean.UserBean;
import connect.activity.wallet.manager.TransferManager;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import com.wallet.inter.WalletListener;

import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.data.RateFormatUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import instant.bean.MsgDirect;
import protos.Connect;

/**
 * Created by puin on 17-8-11.
 */

public class PaymentPresenter implements PaymentContract.Presenter{

    private PaymentContract.BView view;
    private Activity activity;

    public PaymentPresenter(PaymentContract.BView view){
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        activity=view.getActivity();
    }

    @Override
    public void loadPayment(String pubkey) {
        ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(pubkey);
        if (friendEntity == null) {
            UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
            String mypubkey = userBean.getPubKey();
            if (mypubkey.equals(pubkey)) {
                friendEntity = new ContactEntity();
                friendEntity.setAvatar(userBean.getAvatar());
                friendEntity.setUsername(userBean.getName());
                friendEntity.setUid(userBean.getUid());
            } else {
                ActivityUtil.goBack(activity);
                return;
            }
        }

        String avatar = friendEntity.getAvatar();
        String nameTxt = TextUtils.isEmpty(friendEntity.getRemark()) ? friendEntity.getUsername() : friendEntity.getRemark();
        view.showPayment(avatar, nameTxt);
    }

    @Override
    public void loadCrowding(String pubkey) {
        int counts = 0;
        List memberEntities = ContactHelper.getInstance().loadGroupMemEntities(pubkey);
        if (memberEntities != null) {
            counts = memberEntities.size();
        }
        view.showCrowding(counts);
    }

    @Override
    public void requestPayment(CurrencyEnum currencyEnum, final long amount, String tips) {
        TransferManager transferManager = new TransferManager(activity, currencyEnum);
        transferManager.friendReceiver(amount, view.getPubkey(), tips, new WalletListener<Connect.Bill>() {

            @Override
            public void success(Connect.Bill bill) {
                ToastEUtil.makeText(activity, R.string.Wallet_Sent).show();
                MsgSend.sendOuterMsg(MsgSend.MsgSendType.Request_Payment, 0, bill.getHash(), amount, 1, bill.getTips());
                ActivityUtil.goBack(activity);
            }

            @Override
            public void fail(WalletError error) {

            }
        });
    }

    @Override
    public void requestCrowding(CurrencyEnum currencyEnum, long amount, final int size, String tips) {
        long totalamount = amount * size;
        TransferManager transferManager = new TransferManager(activity, currencyEnum);
        transferManager.crowdFuning(view.getPubkey(), totalamount, size, tips, new WalletListener<Connect.Crowdfunding>() {

            @Override
            public void success(Connect.Crowdfunding crowdfunding) {
                MsgSend.sendOuterMsg(MsgSend.MsgSendType.Request_Payment, 1,crowdfunding.getHashId(),crowdfunding.getTotal(),size,crowdfunding.getTips());
                ActivityUtil.goBack(activity);
            }

            @Override
            public void fail(WalletError error) {

            }
        });
    }
}
