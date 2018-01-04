package connect.activity.chat.exts.presenter;

import android.app.Activity;

import java.util.List;

import connect.activity.chat.bean.ContainerBean;
import connect.activity.chat.exts.contract.CrowdingDetailContract;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.ProtoBufUtil;
import connect.utils.UriUtil;
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
                    Connect.HttpNotSignResponse imResponse = Connect.HttpNotSignResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = Connect.StructData.parseFrom(imResponse.getBody());
                    crowdfunding = Connect.Crowdfunding.parseFrom(structData.getPlainData());
                    if (!ProtoBufUtil.getInstance().checkProtoBuf(crowdfunding)) {
                        return;
                    }

                    List<Connect.CrowdfundingRecord> records = crowdfunding.getRecords().getListList();
                    Connect.UserInfo senderInfo = crowdfunding.getSender();
                    String avatar = senderInfo.getAvatar();
                    String senderName = "";
                    if (SharedPreferenceUtil.getInstance().getUser().getUid().equals(senderInfo.getUid())) {
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
                        if (SharedPreferenceUtil.getInstance().getUser().getUid().equals(record.getUser().getUid())) {
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
    public void requestCrowdingPay(final String hashid) {
    }
}
