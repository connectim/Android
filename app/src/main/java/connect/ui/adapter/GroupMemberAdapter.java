package connect.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.ui.activity.contact.FriendInfoActivity;
import connect.ui.activity.contact.StrangerInfoActivity;
import connect.ui.activity.contact.bean.SourceType;
import connect.ui.activity.set.ModifyInfoActivity;
import connect.utils.PinyinUtil;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.SideScrollView;
import connect.view.roundedimageview.RoundedImageView;
import protos.Connect;
/**
 * Created by gtq on 2016/12/15.
 */
public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.MemberReHolder> {

    private RecyclerView recyclerView;
    private SideScrollView sideScrollView = null;
    private Activity activity;
    private LayoutInflater inflater;
    private boolean canScroll = true;
    private List<GroupMemberEntity> groupMemEntities = new ArrayList<>();

    public GroupMemberAdapter(Activity activity, RecyclerView recyclerView) {
        this.activity = activity;
        inflater = LayoutInflater.from(activity);
        this.recyclerView = recyclerView;
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
        String name = TextUtils.isEmpty(memEntity.getNick()) ? memEntity.getUsername() : memEntity.getNick();
        holder.nameTxt.setText(name);
        GlideUtil.loadAvater(holder.headImg, memEntity.getAvatar());

        if (TextUtils.isEmpty(name)) name = "#";
        String curFirst = PinyinUtil.chatToPinyin(name.charAt(0));

        if (position == 0) {
            holder.txt.setVisibility(View.VISIBLE);
            holder.txt.setText(curFirst);
        } else {
            GroupMemberEntity lastEntity = groupMemEntities.get(position - 1);
            String lastName = TextUtils.isEmpty(lastEntity.getNick()) ? lastEntity.getUsername() : lastEntity.getNick();
            if (TextUtils.isEmpty(lastName)) lastName = "#";
            String lastFirst = PinyinUtil.chatToPinyin(lastName.charAt(0));

            if (lastFirst.equals(curFirst)) {
                holder.txt.setVisibility(View.GONE);
            } else {
                holder.txt.setVisibility(View.VISIBLE);
                holder.txt.setText(curFirst);
            }
        }

        if (!canScroll) {
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
                    if (indexEntity.getIdentifier().equals(MemoryDataManager.getInstance().getPubKey())) {
                        ModifyInfoActivity.startActivity(activity);
                    } else {
                        ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(indexEntity.getPub_key());
                        if (friendEntity == null) {
                            StrangerInfoActivity.startActivity(activity, indexEntity.getAddress(), SourceType.GROUP);
                        } else {
                            FriendInfoActivity.startActivity(activity, friendEntity.getPub_key());
                        }
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
            String showName = TextUtils.isEmpty(entity.getNick()) ? entity.getUsername() : entity.getNick();
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

        private RoundedImageView headImg;
        private TextView txt;
        private TextView nameTxt;
        private View contentLayout;
        private View trashLayout;

        public MemberReHolder(View itemView) {
            super(itemView);
            txt= (TextView) itemView.findViewById(R.id.txt);
            headImg = (RoundedImageView) itemView.findViewById(R.id.roundimg);
            nameTxt = (TextView) itemView.findViewById(R.id.tvName);
            contentLayout = itemView.findViewById(R.id.content_layout);
            trashLayout = itemView.findViewById(R.id.bottom_layout);

            ((SideScrollView) itemView).setSideScrollListener(sideScrollListener);
        }
    }

    public void removeData(int position) {
        closeMenu();
        GroupMemberEntity entity = groupMemEntities.get(position);
        if (!MemoryDataManager.getInstance().getPubKey().equals(entity.getPub_key())) {
            removeGroupMember(position ,entity);
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

    protected void removeGroupMember(final int position,final GroupMemberEntity entity) {
        Connect.DelOrQuitGroupMember delMember = Connect.DelOrQuitGroupMember.newBuilder()
                .setIdentifier(entity.getIdentifier()).setAddress(entity.getAddress()).build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_REMOVE, delMember, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                ContactHelper.getInstance().removeMemberEntity(entity.getIdentifier(), entity.getPub_key());

                groupMemEntities.remove(position);
                notifyItemRemoved(position);
                itemRemoveListener.itemRemove(entity);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(activity,R.string.Link_Remove_Member_Failed,ToastEUtil.TOAST_STATUS_FAILE).show();
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
