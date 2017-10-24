package connect.activity.wallet.presenter;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import connect.activity.wallet.contract.PacketDetailContract;
import connect.database.SharedPreferenceUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.UriUtil;
import instant.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

public class PacketDetailPresenter implements PacketDetailContract.Presenter{

    private PacketDetailContract.View mView;
    private Connect.UserInfo sendUserInfo;

    public PacketDetailPresenter(PacketDetailContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void start() {}

    @Override
    public void requestRedDetail(String hashId,int type) {
        Connect.RedPackageHash redPackageHash = Connect.RedPackageHash.newBuilder()
                .setId(hashId)
                .build();
        String uri = (0 == type) ? UriUtil.WALLET_PACKAGE_INFO : UriUtil.WALLET_PACKAGE_SYSTEMINFO;
        OkHttpUtil.getInstance().postEncrySelf(uri, redPackageHash, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.RedPackageInfo redPackageInfo = Connect.RedPackageInfo.parseFrom(structData.getPlainData());
                    requestUserInfo(redPackageInfo);
                    getRedStatus(redPackageInfo);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    private void requestUserInfo(Connect.RedPackageInfo redPackageInfo) {
        Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(redPackageInfo.getRedpackage().getSendAddress())
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    sendUserInfo = Connect.UserInfo.parseFrom(structData.getPlainData());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(sendUserInfo)){
                        mView.updateSendView(sendUserInfo);
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    public void getRedStatus(Connect.RedPackageInfo redPackageInfo) {
        List<Connect.GradRedPackageHistroy> list = redPackageInfo.getGradHistoryList();
        Connect.RedPackage redPackage = redPackageInfo.getRedpackage();
        String uid = SharedPreferenceUtil.getInstance().getUser().getUid();
        int status;
        boolean isHave = false;
        long openMoney = 0;
        long bestAmount = 0;
        for (Connect.GradRedPackageHistroy histroy : list) {
            if (histroy.getUserinfo().getUid().equals(uid)) {
                isHave = true;
                openMoney = histroy.getAmount();
            }
            if(bestAmount == 0 || bestAmount <= histroy.getAmount()){
                bestAmount = histroy.getAmount();
            }
        }

        // Check whether to receive a red envelope
        if(isHave){
            if(TextUtils.isEmpty(redPackage.getTxid())){ // check Txid
                status = 6;
            }else{
                status = 4;
            }
        }else{
            if(redPackage.getRemainSize() == 0){ // Check whether the red envelope is to get out
                status = 5;
            }else if(redPackage.getDeadline() > 0){ // Check whether the timeout
                status = 2;
            }else if(redPackage.getSendAddress().equals(uid)){ // Check for a red envelope sender
                status = 1;
            }else{
                status = 3;
            }
        }
        mView.updateView(status, openMoney, bestAmount, redPackageInfo);
    }

}
