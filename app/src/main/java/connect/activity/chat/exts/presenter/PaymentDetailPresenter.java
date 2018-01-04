package connect.activity.chat.exts.presenter;

import connect.activity.chat.exts.contract.PaymentDetailContract;
import connect.utils.ProtoBufUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/11.
 */

public class PaymentDetailPresenter implements PaymentDetailContract.Presenter{

    private PaymentDetailContract.BView view;

    public PaymentDetailPresenter(PaymentDetailContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void requestPaymentDetail(final String hashid) {
        final Connect.BillHashId hashId = Connect.BillHashId.newBuilder().setHash(hashid).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.BILLING_INFO, hashId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.HttpNotSignResponse imResponse = Connect.HttpNotSignResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = Connect.StructData.parseFrom(imResponse.getBody());
                    Connect.Bill bill = Connect.Bill.parseFrom(structData.getPlainData());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(bill)) {
                        view.showPaymentDetail(bill);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }
}
