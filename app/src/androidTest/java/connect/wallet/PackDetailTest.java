package connect.wallet;

import android.app.Activity;

import org.junit.Test;

import connect.activity.wallet.contract.PacketDetailContract;
import connect.activity.wallet.presenter.PacketDetailPresenter;
import protos.Connect;

import static org.junit.Assert.assertTrue;

/**
 * Created by Administrator on 2017/8/7 0007.
 */

public class PackDetailTest {

    private String Tag = "_PackDetailTest";

    @Test
    public void checkRadPackDetail() throws Exception {
        PacketDetailPresenter presenter = new PacketDetailPresenter(view);
        String address = "13RGdCp32GjxxwJ5YK35e1Z57BtP7sqdXH";
        Connect.UserInfo userInfo = Connect.UserInfo.newBuilder().setUid(address).build();
        Connect.GradRedPackageHistroy packageHistroy = Connect.GradRedPackageHistroy.newBuilder()
                        .setUserinfo(userInfo)
                        .setAmount(100000)
                        .build();
        Connect.RedPackage redPackage = Connect.RedPackage.newBuilder()
                        .setTxid("asdasdasd")
                        .setRemainSize(3)
                        .setDeadline(0)
                        .setSendAddress(address)
                        .build();
        Connect.RedPackageInfo redPackageInfo = Connect.RedPackageInfo.newBuilder()
                .addGradHistory(packageHistroy)
                .setRedpackage(redPackage)
                .build();
        presenter.getRedStatus(redPackageInfo);
    }

    PacketDetailContract.View view = new PacketDetailContract.View(){
        @Override
        public void updataView(int status, long openMoney, long bestAmount, Connect.RedPackageInfo redPackageInfo) {
            assertTrue(status == 3);
        }

        @Override
        public void updataSendView(Connect.UserInfo sendUserInfo) {

        }

        @Override
        public void setPresenter(PacketDetailContract.Presenter presenter) {

        }

        @Override
        public Activity getActivity() {
            return null;
        }
    };

}
