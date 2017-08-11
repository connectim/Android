package connect.activity.chat.exts.presenter;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import connect.activity.chat.exts.contract.GroupCrowdingRecordContract;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/11.
 */

public class GroupCrowdingRecordPresenter implements GroupCrowdingRecordContract.Presenter{

    private GroupCrowdingRecordContract.BView view;

    public GroupCrowdingRecordPresenter(GroupCrowdingRecordContract.BView view){
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void requestGroupCrowdingRecords(int page, int maxsize) {
        Connect.UserCrowdfundingInfo history = Connect.UserCrowdfundingInfo.newBuilder()
                .setPageIndex(page)
                .setPageSize(maxsize)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CROWDFUN_RECORDS, history, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.Crowdfundings crowdfundings = Connect.Crowdfundings.parseFrom(structData.getPlainData());
                    List<Connect.Crowdfunding> list = crowdfundings.getListList();

                    view.crowdingRecords(list);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastUtil.getInstance().showToast(response.getCode() + response.getMessage());
            }
        });
    }
}
