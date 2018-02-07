package connect.activity.chat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseFragment;
import connect.activity.chat.ChatActivity;
import connect.activity.chat.adapter.SearchAdapter;
import connect.activity.chat.fragment.bean.SearchBean;
import connect.activity.contact.ContactInfoActivity;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import protos.Connect;

/**
 * Created by Administrator on 2018/1/31 0031.
 */

public class SearchContentFragment extends BaseFragment {

    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.no_data_lin)
    LinearLayout noDataLin;

    private FragmentActivity mActivity;
    private SearchAdapter adapter;

    public static SearchContentFragment startFragment() {
        SearchContentFragment searchContentFragment = new SearchContentFragment();
        return searchContentFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_chat_content, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
        initView();
    }

    private void initView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        adapter = new SearchAdapter(mActivity);
        //recyclerview.addItemDecoration(new LineDecoration(mActivity, false));
        adapter.setOnItemChildListence(onItemChildClickListener);
        recyclerview.setAdapter(adapter);
    }

    SearchAdapter.OnItemChildClickListener onItemChildClickListener = new SearchAdapter.OnItemChildClickListener(){
        @Override
        public void itemClick(int position, SearchBean searchBean) {
            if (searchBean.getStyle() == 1) {
                ContactInfoActivity.lunchActivity(mActivity, searchBean.getUid());
            } else if (searchBean.getStyle() == 2) {
                ChatActivity.startActivity(mActivity, Connect.ChatType.GROUPCHAT, searchBean.getUid());
            } else if (searchBean.getStyle() == 3) {
                if(searchBean.getStatus() == 1){
                    ChatActivity.startActivity(mActivity, Connect.ChatType.PRIVATE, searchBean.getUid(), searchBean.getSearchStr());
                }else{
                    ChatActivity.startActivity(mActivity, Connect.ChatType.GROUPCHAT, searchBean.getUid(), searchBean.getSearchStr());
                }
            }
        }
    };

    public void updateView(String value, int status){
        ParamManager.getInstance().putCommonlyString(value);
        ArrayList<SearchBean> list = new ArrayList<>();
        if(status == 0){
            list.addAll(getFriendData(value));
            list.addAll(ContactHelper.getInstance().loadGroupByMemberName(value));
            list.addAll(ContactHelper.getInstance().loadGroupByMessages(value));
            list.addAll(ContactHelper.getInstance().loadChatByMessages(value));
        }else if(status == 1){
            list.addAll(getFriendData(value));
        }else if(status == 2){
            list.addAll(ContactHelper.getInstance().loadGroupByMemberName(value));
        } else if(status == 3){
            list.addAll(ContactHelper.getInstance().loadGroupByMessages(value));
            list.addAll(ContactHelper.getInstance().loadChatByMessages(value));
        }

        if(list.size() > 0){
            noDataLin.setVisibility(View.GONE);
            recyclerview.setVisibility(View.VISIBLE);
            adapter.setDataNotify(list);
        }else{
            noDataLin.setVisibility(View.VISIBLE);
            recyclerview.setVisibility(View.GONE);
        }
    }

    private ArrayList<SearchBean> getFriendData(String value){
        ArrayList<SearchBean> list = new ArrayList<>();
        List<ContactEntity> listFriend = ContactHelper.getInstance().loadFriendEntityFromText(value);
        for(ContactEntity contactEntity : listFriend){
            if(!TextUtils.isEmpty(contactEntity.getPublicKey())){
                SearchBean searchBean = new SearchBean();
                searchBean.setStyle(1);
                searchBean.setUid(contactEntity.getUid());
                searchBean.setName(contactEntity.getName());
                searchBean.setAvatar(contactEntity.getAvatar());
                list.add(searchBean);
            }
        }
        return list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
