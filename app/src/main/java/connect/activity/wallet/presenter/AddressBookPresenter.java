package connect.activity.wallet.presenter;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

import connect.database.SharePreferenceUser;
import connect.ui.activity.R;
import connect.activity.wallet.bean.AddressBean;
import connect.activity.wallet.contract.AddressBookContract;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/4/18 0018.
 */

public class AddressBookPresenter implements AddressBookContract.Presenter{

    private AddressBookContract.View mView;
    private ArrayList<AddressBean> listAddress;

    public AddressBookPresenter(AddressBookContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void start() {
        listAddress = SharePreferenceUser.getInstance().getAddressBook();
        if(listAddress == null || listAddress.size() == 0){
            requestAddressBook();
        }else{
            mView.updataView(listAddress);
        }
    }

    @Override
    public void requestAddressBook() {
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_ADDRESS_BOOK_LIST, ByteString.copyFrom(new byte[]{}),
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                            Connect.AddressBook addressBook = Connect.AddressBook.parseFrom(structData.getPlainData());
                            List<Connect.AddressBook.AddressInfo> list = addressBook.getAddressInfoList();

                            ArrayList<Connect.AddressBook.AddressInfo> listCheck = new ArrayList<>();
                            for(Connect.AddressBook.AddressInfo addressInfo : list){
                                if(ProtoBufUtil.getInstance().checkProtoBuf(addressInfo)){
                                    listCheck.add(addressInfo);
                                }
                            }
                            listAddress = switchList(listCheck);
                            mView.updataView(listAddress);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {

                    }
                });
    }

    @Override
    public void requestAddAddress(final String address) {
        if(null == listAddress){
            return;
        }

        for(AddressBean addressBean : listAddress){
            if(addressBean.getAddress().contains(address)){
                ToastEUtil.makeText(mView.getActivity(),R.string.Chat_Address_already_exists).show();
                return;
            }
        }

        Connect.AddressBook.AddressInfo addressInfo = Connect.AddressBook.AddressInfo.newBuilder()
                .setAddress(address)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_ADDRESS_BOOK_ADD, addressInfo, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                listAddress.add(0,new AddressBean("",address));
                ToastEUtil.makeText(mView.getActivity(), R.string.Link_Add_Successful);
                mView.updataView(listAddress);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(mView.getActivity(),R.string.Link_Add_Failed,ToastEUtil.TOAST_STATUS_FAILE);
            }
        });
    }

    @Override
    public void requestSetTag(final String address, final String tag, final int position) {
        Connect.AddressBook.AddressInfo addressInfo = Connect.AddressBook.AddressInfo.newBuilder()
                .setAddress(address)
                .setTag(tag)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_ADDRESS_BOOK_TAG, addressInfo, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                listAddress.remove(position);
                listAddress.add(position,new AddressBean(tag,address));
                mView.updataView(listAddress);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(mView.getActivity(),R.string.Set_Setting_Faied,ToastEUtil.TOAST_STATUS_FAILE);
            }
        });
    }

    @Override
    public void requestRemove(String address, final int position) {
        Connect.AddressBook.AddressInfo addressInfo = Connect.AddressBook.AddressInfo.newBuilder()
                .setAddress(address)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_ADDRESS_BOOK_REMOVE, addressInfo, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                listAddress.remove(position);
                mView.updataView(listAddress);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if(response.getCode() == 2400){
                    listAddress.remove(position);
                    mView.updataView(listAddress);
                }
            }
        });
    }

    private ArrayList<AddressBean> switchList(List<Connect.AddressBook.AddressInfo> listAddress){
        ArrayList list = new ArrayList<AddressBean>();
        for(Connect.AddressBook.AddressInfo addressInfo : listAddress){
            AddressBean addressBean = new AddressBean(addressInfo.getTag(),addressInfo.getAddress());
            list.add(addressBean);
        }
        return list;
    }

}
