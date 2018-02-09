package connect.activity.chat.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.activity.contact.ContactInfoActivity;
import connect.activity.set.UserInfoActivity;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.utils.PinyinUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.utils.system.SystemDataUtil;
import connect.widget.SideScrollView;
import protos.Connect;

/**
 * Created by gtq on 2016/12/15.
 */
public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.MemberReHolder> {

    private SideScrollView sideScrollView = null;
    private Activity activity;
    private LayoutInflater inflater;
    private boolean canScroll = true;
    private List<GroupMemberEntity> groupMemEntities = new ArrayList<>();

    public GroupMemberAdapter(Activity activity, List<GroupMemberEntity> entities) {
        this.activity = activity;
        inflater = LayoutInflater.from(activity);
        this.groupMemEntities = entities;
    }

    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }

    public void setData(List<GroupMemberEntity> entities) {
        this.groupMemEntities = entities;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return groupMemEntities.size();
    }

    @Override
    public MemberReHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
        View view = inflater.inflate(R.layout.item_group_memre, arg0, false);
        MemberReHolder holder = new MemberReHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MemberReHolder holder, final int position) {
        GroupMemberEntity memEntity = groupMemEntities.get(position);
        String name = TextUtils.isEmpty(memEntity.getUsername()) ? "" : memEntity.getUsername();
        holder.nameTxt.setText(name);
        GlideUtil.loadAvatarRound(holder.headImg, memEntity.getAvatar());

        if (TextUtils.isEmpty(name)) name = "#";
        String curFirst = PinyinUtil.chatToPinyin(name.charAt(0));

        if (position == 0) {
            holder.txt.setVisibility(View.VISIBLE);
            holder.txt.setText(curFirst);
        } else {
            GroupMemberEntity lastEntity = groupMemEntities.get(position - 1);
            String lastName = TextUtils.isEmpty(lastEntity.getUsername()) ? "" : lastEntity.getUsername();
            if (TextUtils.isEmpty(lastName)) lastName = "#";
            String lastFirst = PinyinUtil.chatToPinyin(lastName.charAt(0));

            if (lastFirst.equals(curFirst)) {
                holder.txt.setVisibility(View.GONE);
            } else {
                holder.txt.setVisibility(View.VISIBLE);
                holder.txt.setText(curFirst);
            }
        }

        if (canScroll) {
            String myUid = SharedPreferenceUtil.getInstance().getUser().getUid();
            boolean isMyEntity = myUid.equals(memEntity.getUid());
            holder.trashLayout.setVisibility(isMyEntity ? View.GONE : View.VISIBLE);
        } else {
            holder.trashLayout.setVisibility(View.GONE);
        }
        holder.contentLayout.getLayoutParams().width = SystemDataUtil.getScreenWidth();
        holder.contentLayout.setTag(holder.itemView);
        holder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SideScrollView scrollView = (SideScrollView) v.getTag();
                if (menuIsOpen(scrollView) || menuIsOpen(sideScrollView)) {
                    closeMenu(scrollView);
                    closeMenu();
                } else {
                    closeMenu();
                    GroupMemberEntity indexEntity = groupMemEntities.get(position);
                    if (indexEntity.getUid().equals(SharedPreferenceUtil.getInstance().getUser().getUid())) {
                        UserInfoActivity.startActivity(activity);
                    } else {
                        ContactInfoActivity.lunchActivity(activity, indexEntity.getUid());
                    }
                }
            }
        });

        holder.trashLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int posi = holder.getLayoutPosition();
                removeData(posi);
            }
        });
    }

    public int getPositionForSection(char selectchar) {
        for (int i = 0; i < groupMemEntities.size(); i++) {
            GroupMemberEntity entity = groupMemEntities.get(i);
            String showName = entity.getUsername();
            if (TextUtils.isEmpty(showName)) {
                showName = "#";
            }
            String firstChar = PinyinUtil.chatToPinyin(showName.charAt(0));
            if (firstChar.charAt(0) >= selectchar) {
                return i;
            }
        }
        return -1;
    }

    class MemberReHolder extends RecyclerView.ViewHolder {

        private ImageView headImg;
        private TextView txt;
        private TextView nameTxt;
        private View contentLayout;
        private View trashLayout;

        public MemberReHolder(View itemView) {
            super(itemView);
            txt = (TextView) itemView.findViewById(R.id.txt);
            headImg = (ImageView) itemView.findViewById(R.id.roundimg);
            nameTxt = (TextView) itemView.findViewById(R.id.tvName);
            contentLayout = itemView.findViewById(R.id.content_layout);
            trashLayout = itemView.findViewById(R.id.bottom_layout);

            ((SideScrollView) itemView).setSideScrollListener(sideScrollListener);
        }
    }

    public void removeData(int position) {
        closeMenu();
        GroupMemberEntity entity = groupMemEntities.get(position);
        if (!SharedPreferenceUtil.getInstance().getUser().getUid().equals(entity.getUid())) {
            removeGroupMember(position, entity);
        }
    }

    public void closeMenu() {
        closeMenu(sideScrollView);
        sideScrollView = null;
    }

    public void closeMenu(SideScrollView scrollView) {
        if (scrollView != null) {
            scrollView.closeMenu();
        }
    }

    public Boolean menuIsOpen(SideScrollView scrollView) {
        return scrollView != null && scrollView.isOpen();
    }

    protected void removeGroupMember(final int position, final GroupMemberEntity entity) {
        Connect.DelOrQuitGroupMember delMember = Connect.DelOrQuitGroupMember.newBuilder()
                .setIdentifier(entity.getIdentifier())
                .setUid(entity.getUid())
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_REMOVE, delMember, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                ContactHelper.getInstance().removeMemberEntity(entity.getIdentifier(), entity.getUid());

                groupMemEntities.remove(position);
                notifyItemRemoved(position);
                itemRemoveListener.itemRemove(entity);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if (response.getCode() == 2423) {
                    ToastEUtil.makeText(activity, R.string.Chat_Not_Group_Master, ToastEUtil.TOAST_STATUS_FAILE).show();
                } else {
                    ToastEUtil.makeText(activity, response.getMessage(), ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        });
    }


    private OnItemRemoveListener itemRemoveListener;

    public interface OnItemRemoveListener {
        void itemRemove(GroupMemberEntity entity);
    }

    public void setItemRemoveListener(OnItemRemoveListener itemRemoveListener) {
        this.itemRemoveListener = itemRemoveListener;
    }

    private SideScrollView.SideScrollListener sideScrollListener = new SideScrollView.SideScrollListener() {

        @Override
        public void onMenuIsOpen(View view) {
            sideScrollView = (SideScrollView) view;
        }

        @Override
        public void onDownOrMove(SideScrollView slidingButtonView) {
            if (menuIsOpen(sideScrollView)) {
                if (sideScrollView != slidingButtonView) {
                    closeMenu();
                }
            }
        }
    };
}
