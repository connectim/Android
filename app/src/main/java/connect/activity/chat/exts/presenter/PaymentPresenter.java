package connect.activity.chat.exts.presenter;

import android.app.Activity;
import android.text.TextUtils;

import java.util.List;

import connect.activity.chat.bean.GatherBean;
import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.exts.contract.PaymentContract;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.business.BaseBusiness;
import connect.wallet.cwallet.inter.WalletListener;
import protos.Connect;

/**
 * Created by puin on 17-8-11.
 */

public class PaymentPresenter implements PaymentContract.Presenter{

    private PaymentContract.BView view;
    private Activity activity;

    public PaymentPresenter(PaymentContract.BView view){
        this.view=view;
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void loadPayment(String pubkey) {
        ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(pubkey);
        if (friendEntity == null) {
            String mypubkey = MemoryDataManager.getInstance().getPubKey();
            if (mypubkey.equals(pubkey)) {
                friendEntity = new ContactEntity();
                friendEntity.setAvatar(MemoryDataManager.getInstance().getAvatar());
                friendEntity.setUsername(MemoryDataManager.getInstance().getName());
                friendEntity.setAddress(MemoryDataManager.getInstance().getAddress());
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
        List memberEntities = ContactHelper.getInstance().loadGroupMemEntity(pubkey);
        if (memberEntities != null) {
            counts = memberEntities.size();
        }
        view.showCrowding(counts);
    }

    @Override
    public void requestPayment(CurrencyEnum currencyEnum,final long amount,String tips) {
        BaseBusiness baseBusiness = new BaseBusiness(activity, currencyEnum);
        baseBusiness.friendReceiver(amount, view.getPubkey(), tips, new WalletListener<Connect.Bill>() {

            @Override
            public void success(Connect.Bill bill) {
                ToastEUtil.makeText(activity, R.string.Wallet_Sent).show();
                MsgSend.sendOuterMsg(MsgType.Request_Payment, 0, bill.getHash(), amount, 1, bill.getTips());
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
        BaseBusiness baseBusiness = new BaseBusiness(activity, currencyEnum);
        baseBusiness.crowdFuning(view.getPubkey(), totalamount, size, tips, new WalletListener<Connect.Crowdfunding>() {

            @Override
            public void success(Connect.Crowdfunding crowdfunding) {
                GatherBean gatherBean = new GatherBean(crowdfunding.getHashId(), crowdfunding.getTotal() / size, size, true, crowdfunding.getTips());
                MsgSend.sendOuterMsg(MsgType.Request_Payment, gatherBean);
                ActivityUtil.goBack(activity);
            }

            @Override
            public void fail(WalletError error) {

            }
        });
    }
}
