package connect.activity.chat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import connect.ui.activity.R;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/14.
 */

public class TransferMutiDetailAdapter extends RecyclerView.Adapter<TransferMutiDetailAdapter.MutiDetailHolder>{

    private Context context;
    private String[] receivers;
    private long amount;

    public TransferMutiDetailAdapter(String[] receivers, long amount) {
        this.receivers = receivers;
        this.amount = amount;
    }

    public void updateData(String[] receivers, long amount) {
        this.receivers = receivers;
        this.amount = amount;
        notifyDataSetChanged();
    }

    @Override
    public MutiDetailHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_multitransfer_detail, parent, false);
        MutiDetailHolder mutiDetailHolder = new MutiDetailHolder(view);
        view.setOnClickListener(itemClickListener);
        return mutiDetailHolder;
    }

    @Override
    public int getItemCount() {
        return receivers.length;
    }

    @Override
    public void onBindViewHolder(final MutiDetailHolder holder, int position) {
//        String address = SupportKeyUril.getAddressFromPubKey(receivers[position]);
        String address="";
        Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(address)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser,
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.HttpNotSignResponse imResponse = Connect.HttpNotSignResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = Connect.StructData.parseFrom(imResponse.getBody());
                            Connect.UserInfo userInfo = Connect.UserInfo.parseFrom(structData.getPlainData());
                            if (ProtoBufUtil.getInstance().checkProtoBuf(userInfo)) {
                                String avatar = userInfo.getAvatar();
                                String name = userInfo.getUsername();

                                GlideUtil.loadAvatarRound(holder.avatarImg, avatar);
                                holder.receiverTxt.setText(name);
                                holder.amountTxt.setText(context.getString(R.string.Set_BTC_symbol) + "" + RateFormatUtil.longToDoubleBtc(amount));
                            }
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

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener extends View.OnClickListener {
    }

    public void setItemClickListener(OnItemClickListener clickListener) {
        this.itemClickListener = clickListener;
    }

    static class MutiDetailHolder extends RecyclerView.ViewHolder {

        private ImageView avatarImg;
        private TextView receiverTxt;
        private TextView amountTxt;

        MutiDetailHolder(View view) {
            super(view);
            avatarImg = (ImageView) view.findViewById(R.id.roundimg);
            receiverTxt = (TextView) view.findViewById(R.id.txt1);
            amountTxt = (TextView) view.findViewById(R.id.txt2);
        }
    }
}
