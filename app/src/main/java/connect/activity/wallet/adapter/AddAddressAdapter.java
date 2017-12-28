package connect.activity.wallet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.activity.wallet.bean.AddressBean;
import connect.ui.activity.R;
import connect.utils.ToastEUtil;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;
import connect.widget.SideScrollView;

public class AddAddressAdapter extends RecyclerView.Adapter<AddAddressAdapter.AddressHolder> {

    private List<AddressBean> list = new ArrayList<>();
    private SideScrollView sideScrollView;
    private onSideMenuListener onSideMenuListener;
    private Context context;
    private Context mContext;

    public AddAddressAdapter(onSideMenuListener onSideMenuListener) {
        this.onSideMenuListener = onSideMenuListener;
    }

    public void setData(List<AddressBean> entities) {
        this.list.clear();
        this.list.add(0,new AddressBean());
        if(entities != null){
            this.list.addAll(entities);
        }
        notifyDataSetChanged();
    }

    @Override
    public AddAddressAdapter.AddressHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet_sele_address, parent, false);
        return new AddressHolder(view);
    }

    @Override
    public void onBindViewHolder(final AddAddressAdapter.AddressHolder holder, final int position) {
        holder.contentLayout.getLayoutParams().width = SystemDataUtil.getScreenWidth() - SystemUtil.dipToPx(20);

        if(position == 0){
            holder.sideScrollView.setVisibility(View.GONE);
            holder.topRela.setVisibility(View.VISIBLE);
            holder.topRela.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(holder.addImg.getVisibility() == View.VISIBLE){
                        holder.addImg.setVisibility(View.GONE);
                        holder.addEt.setVisibility(View.VISIBLE);
                    }else{
                        holder.addImg.setVisibility(View.VISIBLE);
                        holder.addEt.setVisibility(View.GONE);
                    }
                }
            });
            holder.addEt.setText("");
            holder.addEt.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                        SystemUtil.hideKeyBoard(context,holder.addEt);
                        String address = holder.addEt.getText().toString().trim();
//                        if(SupportKeyUril.checkAddress(address)){
//                            onSideMenuListener.addAddress(address);
//                        }else{
//                            ToastEUtil.makeText(mContext,R.string.Wallet_Result_is_not_a_bitcoin_address,ToastEUtil.TOAST_STATUS_FAILE).show();
//                        }
                    }
                    return false;
                }
            });
        }else {
            holder.sideScrollView.setVisibility(View.VISIBLE);
            holder.topRela.setVisibility(View.GONE);
            holder.addressTv.setText(list.get(position).getAddress());
            holder.tagTv.setText(list.get(position).getTag());
            holder.contentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSideMenuListener.seleAddress(position,list.get(position));
                }
            });
            holder.labTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = position - 1;
                    onSideMenuListener.setTag(index,list.get(position));
                }
            });

            holder.delTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = position - 1;
                    onSideMenuListener.delete(index,list.get(position));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class AddressHolder extends RecyclerView.ViewHolder{
        private final RelativeLayout topRela;
        private final ImageView addImg;
        private final EditText addEt;

        private final SideScrollView sideScrollView;
        private final RelativeLayout contentLayout;
        private final LinearLayout bottomLayout;
        private final TextView addressTv;
        private final TextView tagTv;
        private final TextView labTv;
        private final TextView delTv;

        public AddressHolder(View itemView) {
            super(itemView);
            topRela = (RelativeLayout)itemView.findViewById(R.id.top_rela);
            addImg = (ImageView)itemView.findViewById(R.id.add_img);
            addEt = (EditText)itemView.findViewById(R.id.add_et);

            contentLayout = (RelativeLayout)itemView.findViewById(R.id.content_layout);
            bottomLayout = (LinearLayout)itemView.findViewById(R.id.bottom_layout);
            sideScrollView = (SideScrollView)itemView.findViewById(R.id.side_scroll_view);
            addressTv = (TextView)itemView.findViewById(R.id.address_tv);
            tagTv = (TextView)itemView.findViewById(R.id.tag_tv);
            labTv = (TextView)itemView.findViewById(R.id.lab_tv);
            delTv = (TextView)itemView.findViewById(R.id.del_tv);

            sideScrollView.setSideScrollListener(sideScrollListener);
        }
    }


    public void removeData(int position) {
        list.remove(position);
        notifyItemRemoved(position);
    }

    public void closeMenu() {
        if (sideScrollView != null) {
            sideScrollView.closeMenu();
            sideScrollView = null;
        }
    }

    public Boolean menuIsOpen() {
        return sideScrollView != null;
    }


    private SideScrollView.SideScrollListener sideScrollListener = new SideScrollView.SideScrollListener() {

        @Override
        public void onMenuIsOpen(View view) {
            sideScrollView = (SideScrollView) view;
        }

        @Override
        public void onDownOrMove(SideScrollView slidingButtonView) {
            if (menuIsOpen()) {
                if (sideScrollView != slidingButtonView) {
                    closeMenu();
                }
            }
        }
    };

    public interface onSideMenuListener {
        void seleAddress(int position, AddressBean addressBean);

        void setTag(int position, AddressBean addressBean);

        void delete(int position, AddressBean addressBean);

        void addAddress(String address);
    }

}
