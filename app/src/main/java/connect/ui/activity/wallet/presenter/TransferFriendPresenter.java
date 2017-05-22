package connect.ui.activity.wallet.presenter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.ConversionHelper;
import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.DaoHelper.ParamManager;
import connect.db.green.DaoHelper.TransactionHelper;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.ConversionEntity;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.model.content.FriendChat;
import connect.ui.activity.chat.model.content.NormalChat;
import connect.ui.activity.wallet.TransferActivity;
import connect.ui.activity.wallet.TransferFriendSeleActivity;
import connect.ui.activity.wallet.bean.TransferBean;
import connect.ui.activity.wallet.contract.TransferFriendContract;
import connect.ui.activity.wallet.support.TransferError;
import connect.ui.base.BaseApplication;
import connect.utils.ActivityUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.system.SystemUtil;
import connect.utils.TimeUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.MdStyleProgress;
import connect.view.payment.PaymentPwd;
import connect.view.transferEdit.TransferEditView;
import protos.Connect;

/**
 * Created by Administrator on 2017/4/18 0018.
 */
public class TransferFriendPresenter implements TransferFriendContract.Presenter{

    private TransferFriendContract.View mView;
    private List<ContactEntity> list;

    public TransferFriendPresenter(TransferFriendContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void start() {

    }

    @Override
    public void setListData(List<ContactEntity> list) {
        this.list = list;
    }

    @Override
    public List<ContactEntity> getListFriend() {
        return list;
    }

    @Override
    public AdapterView.OnItemClickListener getItemClickListener() {
        return onItemClickListener;
    }

    @Override
    public TransferEditView.OnEditListener getOnEditListener() {
        return onEditListener;
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ContactEntity friendEntity = (ContactEntity) parent.getAdapter().getItem(position);
            if (TextUtils.isEmpty(friendEntity.getAvatar())) {
                mView.addTranferFriend();
            }
        }
    };

    private TransferEditView.OnEditListener onEditListener = new TransferEditView.OnEditListener() {
        @Override
        public void onEdit(String value) {
            if (TextUtils.isEmpty(value) || list.size() == 0 ||
                    Double.valueOf(mView.getCurrentBtc()) < 0.0001) {
                mView.setBtnEnabled(false);
            } else {
                mView.setBtnEnabled(true);
            }
        }

        @Override
        public void setFee() {
            mView.setPayFee();
        }
    };

    @Override
    public void checkBtnEnable(){
        if (!TextUtils.isEmpty(mView.getCurrentBtc())
                && list.size() > 0
                && Double.valueOf(mView.getCurrentBtc()) >= 0.0001) {
            mView.setBtnEnabled(true);
        } else {
            mView.setBtnEnabled(false);
        }
    }

    @Override
    public void horizontal_layout(GridView gridView) {
        int spacingH = SystemUtil.dipToPx(10);
        int itemWidth = SystemUtil.dipToPx(45);
        int size = list.size() + 1;
        int allWidth = ((spacingH + itemWidth) * size);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                allWidth, LinearLayout.LayoutParams.FILL_PARENT);
        gridView.setLayoutParams(params);
        gridView.setColumnWidth(itemWidth);
        gridView.setHorizontalSpacing(spacingH);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setNumColumns(size);
    }

    @Override
    public void requestSend(long amount, String samValue, final String note, final PaymentPwd paymentPwd) {
        Connect.MuiltSendBill.Builder builder = Connect.MuiltSendBill.newBuilder();
        for (ContactEntity friendEntity : list) {
            builder.addAddresses(friendEntity.getAddress());
        }
        builder.setAmount(list.size() * amount);
        if (!TextUtils.isEmpty(note)) {
            builder.setTips(note);
        }
        builder.setTxData(samValue);
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_BILLING_MUILT_SEND, builder.build(),
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            final Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(SharedPreferenceUtil.getInstance().getPriKey(), imResponse.getCipherData());
                            paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadSuccess, new PaymentPwd.OnAnimationListener() {
                                @Override
                                public void onComplete() {
                                    try {
                                        Connect.MuiltSendBillResp muiltSendBillResp = Connect.MuiltSendBillResp.parseFrom(structData.getPlainData());
                                        List<Connect.Bill> bills = muiltSendBillResp.getBillsList();
                                        for (Connect.Bill bill : bills) {
                                            transferToFriend(bill.getHash(), bill.getReceiver(),note);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    for(ContactEntity friendEntity : list){
                                        ParamManager.getInstance().putLatelyTransfer(new TransferBean(4,friendEntity.getAvatar(),
                                                friendEntity.getUsername(),friendEntity.getAddress()));
                                    }
                                    List<Activity> list = BaseApplication.getInstance().getActivityList();
                                    for (Activity activity : list) {
                                        if (activity.getClass().getName().equals(TransferActivity.class.getName())) {
                                            activity.finish();
                                        }
                                        if (activity.getClass().getName().equals(TransferFriendSeleActivity.class.getName())) {
                                            activity.finish();
                                        }
                                    }
                                    ActivityUtil.goBack(mView.getActivity());
                                }
                            });
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {
                        paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadFail);
                        TransferError.getInstance().showError(response.getCode(),response.getMessage());
                    }
                });
    }

    private void transferToFriend(String hashid, String address,String note) {
        ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(address);
        if (friendEntity != null) {
            NormalChat friendChat = new FriendChat(friendEntity);
            long amount = RateFormatUtil.stringToLongBtc(mView.getCurrentBtc());
            MsgEntity msgEntity = friendChat.transferMsg(hashid, amount, note,0);
            MessageHelper.getInstance().insertToMsg(msgEntity.getMsgDefinBean());

            friendChat.sendPushMsg(msgEntity);

            ConversionEntity roomEntity = ConversionHelper.getInstance().loadRoomEnitity(friendEntity.getPub_key());
            if (roomEntity == null) {
                roomEntity = new ConversionEntity();
                roomEntity.setIdentifier(friendEntity.getPub_key());
                roomEntity.setAvatar(friendEntity.getAvatar());
                roomEntity.setName(friendEntity.getUsername());
                roomEntity.setType(0);
            }

            roomEntity.setContent(mView.getActivity().getString(R.string.Chat_Transfer));
            roomEntity.setLast_time(TimeUtil.getCurrentTimeInLong());
            ConversionHelper.getInstance().insertRoomEntity(roomEntity);

            TransactionHelper.getInstance().updateTransEntity(hashid, msgEntity.getMsgid(), 1);
        }
    }

}
