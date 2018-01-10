package connect.activity.chat.exts.presenter;

import android.app.Activity;
import android.text.TextUtils;

import java.util.List;

import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.exts.contract.PaymentContract;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
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
            String mypubkey = userBean.getUid();
            if (mypubkey.equals(pubkey)) {
                friendEntity = new ContactEntity();
                friendEntity.setAvatar(userBean.getAvatar());
                friendEntity.setName(userBean.getName());
                friendEntity.setUid(userBean.getUid());
            } else {
                ActivityUtil.goBack(activity);
                return;
            }
        }

        String avatar = friendEntity.getAvatar();
        String nameTxt = TextUtils.isEmpty(friendEntity.getRemark()) ? friendEntity.getName() : friendEntity.getRemark();
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

}
