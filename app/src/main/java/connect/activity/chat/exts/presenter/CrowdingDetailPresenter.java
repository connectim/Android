package connect.activity.chat.exts.presenter;

import android.app.Activity;

import com.wallet.bean.CurrencyEnum;
import com.wallet.inter.WalletListener;

import java.util.List;

import connect.activity.chat.bean.ContainerBean;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.exts.contract.CrowdingDetailContract;
import connect.activity.chat.model.content.GroupChat;
import connect.activity.chat.model.content.NormalChat;
import connect.activity.wallet.manager.TransferManager;
import connect.activity.wallet.manager.TransferType;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.database.green.bean.CurrencyEntity;
import connect.database.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.data.RateFormatUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/11.
 */

public class CrowdingDetailPresenter implements CrowdingDetailContract.Presenter{

    private CrowdingDetailContract.BView view;
    private Activity activity;
    private Connect.Crowdfunding crowdfunding = null;

    public CrowdingDetailPresenter(CrowdingDetailContract.BView view){
        this.view=view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        activity = view.getActivity();
    }

    @Override
    public void requestCrowdingDetail(final String hashid) {
        Connect.BillHashId hashId = Connect.BillHashId.newBuilder()
                .setHash(hashid).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CROWDFUN_INFO, hashId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }

                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    crowdfunding = Connect.Crowdfunding.parseFrom(structData.getPlainData());
                    if (!ProtoBufUtil.getInstance().checkProtoBuf(crowdfunding)) {
                        return;
                    }

                    List<Connect.CrowdfundingRecord> records = crowdfunding.getRecords().getListList();
                    Connect.UserInfo senderInfo = crowdfunding.getSender();
                    String avatar = senderInfo.getAvatar();
                    String senderName = "";
                    if (MemoryDataManager.getInstance().getAddress().equals(senderInfo.getUid())) {
                        senderName = activity.getString(R.string.Chat_You);
                    } else {
                        senderName = senderInfo.getUsername();
                        CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(0);
                        if (currencyEntity != null) {
                            view.showBalance(currencyEntity.getBalance());
                        }
                    }
                    view.senderInfo(avatar, senderName);
                    view.showTips(crowdfunding.getTips());

                    long eachamont = crowdfunding.getTotal() / crowdfunding.getSize();
                    view.showCrowdingInfo(crowdfunding.getTotal(), eachamont, (int) crowdfunding.getStatus(), senderInfo.getUid());

                    int payMemCount = (int) (crowdfunding.getSize() - crowdfunding.getRemainSize());
                    int crowdSize = (int) crowdfunding.getSize();
                    String paidinfo = String.format(activity.getString(R.string.Wallet_members_paid_BTC), payMemCount, crowdSize, "" + RateFormatUtil.longToDoubleBtc(payMemCount * eachamont));
                    view.showPaidInfo(paidinfo);

                    String messageid = view.getMessageId();
                    TransactionHelper.getInstance().updateTransEntity(hashid, messageid, payMemCount, crowdSize);
                    ContainerBean.sendRecExtMsg(ContainerBean.ContainerType.GATHER_DETAIL, messageid, 1, payMemCount, crowdSize);

                    boolean state = false;
                    for (Connect.CrowdfundingRecord record : records) {
                        if (MemoryDataManager.getInstance().getAddress().equals(record.getUser().getUid())) {
                            state = true;
                        }
                    }
                    view.showCrowdingRecords(records, state);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    @Override
    public void requestCrowdingPay(String hashid) {
        TransferManager transferManager = new TransferManager(activity, CurrencyEnum.BTC);
        transferManager.typePayment(hashid, TransferType.TransactionTypePayment.getType(), new WalletListener<String>() {
            @Override
            public void success(String hashId) {
                String contactName = crowdfunding.getSender().getUsername();
                String noticeContent = activity.getString(R.string.Chat_paid_the_crowd_founding_to, activity.getString(R.string.Chat_You), contactName);
                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.NOTICE, noticeContent);

                GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(crowdfunding.getGroupHash());
                if (groupEntity != null) {
                    NormalChat normalChat = new GroupChat(groupEntity);
                    MsgExtEntity msgExtEntity = normalChat.noticeMsg(noticeContent);
                    MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                }

                String hashid = crowdfunding.getHashId();
                int paycount = (int) (crowdfunding.getSize() - crowdfunding.getRemainSize());
                int crowdcount = (int) crowdfunding.getSize();

                String messageid = view.getMessageId();
                TransactionHelper.getInstance().updateTransEntity(hashid, messageid, paycount, crowdcount);

                ContainerBean.sendRecExtMsg(ContainerBean.ContainerType.GATHER_DETAIL, messageid, 1, paycount, crowdcount);
                ToastEUtil.makeText(activity, activity.getString(R.string.Wallet_Payment_Successful), 1, new ToastEUtil.OnToastListener() {
                    @Override
                    public void animFinish() {
                        ActivityUtil.goBack(activity);
                    }
                }).show();
            }

            @Override
            public void fail(WalletError error) {

            }
        });
    }
}
