package connect.ui.activity.wallet.presenter;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import connect.db.SharedPreferenceUtil;
import connect.ui.activity.wallet.contract.PacketDetailContract;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/4/18 0018.
 */
public class PacketDetailPresenter implements PacketDetailContract.Presenter{

    private PacketDetailContract.View mView;
    private Connect.RedPackageInfo redPackageInfo;
    private Connect.UserInfo sendUserInfo;

    public PacketDetailPresenter(PacketDetailContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void start() {

    }

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
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(SharedPreferenceUtil.getInstance().getPriKey(), imResponse.getCipherData());
                    redPackageInfo = Connect.RedPackageInfo.parseFrom(structData.getPlainData());
                    requestUserInfo();
                    getRedStatus();
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    private void requestUserInfo() {
        Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(redPackageInfo.getRedpackage().getSendAddress())
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(SharedPreferenceUtil.getInstance().getPriKey(), imResponse.getCipherData());
                    sendUserInfo = Connect.UserInfo.parseFrom(structData.getPlainData());
                    mView.updataSendView(sendUserInfo);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    private void getRedStatus() {
        int status = 0;
        List<Connect.GradRedPackageHistroy> list = redPackageInfo.getGradHistoryList();
        Connect.RedPackage redPackage = redPackageInfo.getRedpackage();
        String address = SharedPreferenceUtil.getInstance().getAddress();

        if (redPackage.getDeadline() < 0) {
            if (redPackage.getSendAddress().equals(address)) {
                if (TextUtils.isEmpty(redPackage.getTxid())) {
                    status = 1;
                } else {
                    status = 2;
                }
            } else {
                status = 3;
            }
        } else {
            if (redPackage.getRemainSize() == redPackage.getSize()) {
                status = 4;
            }

            if (redPackage.getRemainSize() == 0 &&
                    !redPackage.getSendAddress().equals(address)) {//Good luck next time
                status = 5;
            }
        }

        boolean isHava = false;
        long openMoney = 0;
        for (Connect.GradRedPackageHistroy histroy : list) {
            if (histroy.getUserinfo().getAddress().equals(address)) {
                isHava = true;
                openMoney = histroy.getAmount();
            }
        }

        if (isHava && !TextUtils.isEmpty(redPackage.getTxid())) {//Lucky packet transfering to your wallet
            status = 6;
        }
        mView.updataView(status,openMoney,redPackageInfo);
    }

}
