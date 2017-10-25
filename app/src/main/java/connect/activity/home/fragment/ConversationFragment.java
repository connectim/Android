package connect.activity.home.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseFragment;
import connect.activity.home.HomeActivity;
import connect.activity.home.adapter.ConversationAdapter;
import connect.activity.home.bean.ConversationAction;
import connect.activity.home.bean.RoomAttrBean;
import connect.activity.home.view.ConnectStateView;
import connect.activity.home.view.LineDecoration;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.ui.activity.R;
import connect.utils.log.LogManager;
/**
 * Created by gtq on 2016/11/21.
 */
public class ConversationFragment extends BaseFragment {

    @Bind(R.id.recycler_fragment_chat)
    RecyclerView recyclerFragmentChat;
    @Bind(R.id.connectstate)
    ConnectStateView connectStateView;

    private String Tag = "_ConversationFragment";
    private Activity activity;
    private View view;

    private ConversationAdapter chatFragmentAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogManager.getLogger().d(Tag, "onCreateView()");
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_chat, container, false);
            ButterKnife.bind(this, view);
        }
        return view;
    }

    public static ConversationFragment startFragment() {
        ConversationFragment chatListFragment = new ConversationFragment();
        return chatListFragment;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ConversationAction action) {
        LogManager.getLogger().d(Tag, "onEventMainThread()");
        loadRooms();
    }

    /**
     * Query chat message list
     */
    protected void loadRooms() {
        new AsyncTask<Void, Void, List<RoomAttrBean>>() {

            @Override
            protected List<RoomAttrBean> doInBackground(Void... params) {
                return ConversionHelper.getInstance().loadRoomEntites();
            }

            @Override
            protected void onPostExecute(List<RoomAttrBean> entities) {
                super.onPostExecute(entities);
                chatFragmentAdapter.setData(entities);

                int unreadCount = ConversionHelper.getInstance().countUnReads();
                ((HomeActivity) activity).setFragmentDot(0, unreadCount);
            }
        }.execute();
    }

    /**
     * The view has been created
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogManager.getLogger().d(Tag, "onActivityCreated()");
        activity = getActivity();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerFragmentChat.setLayoutManager(linearLayoutManager);
        chatFragmentAdapter = new ConversationAdapter(activity, recyclerFragmentChat);
        recyclerFragmentChat.setAdapter(chatFragmentAdapter);
        recyclerFragmentChat.addItemDecoration(new LineDecoration(activity));
        recyclerFragmentChat.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                chatFragmentAdapter.closeMenu();
            }
        });

        loadRooms();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogManager.getLogger().d(Tag, "onDestroyView()");
        ButterKnife.unbind(this);
        EventBus.getDefault().unregister(this);
    }
}
