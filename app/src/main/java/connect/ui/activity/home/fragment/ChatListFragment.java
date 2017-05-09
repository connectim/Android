package connect.ui.activity.home.fragment;

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
import connect.db.green.DaoHelper.ConversionHelper;
import connect.ui.activity.R;
import connect.ui.activity.home.HomeActivity;
import connect.ui.activity.home.bean.MsgFragmReceiver;
import connect.ui.activity.home.bean.RoomAttrBean;
import connect.ui.activity.home.view.ConnectStateView;
import connect.ui.activity.home.view.LineDecoration;
import connect.ui.adapter.ChatListAdapter;
import connect.ui.base.BaseFragment;
import connect.utils.log.LogManager;
/**
 * Created by gtq on 2016/11/21.
 */
public class ChatListFragment extends BaseFragment {
    @Bind(R.id.recycler_fragment_chat)
    RecyclerView recyclerFragmentChat;
    @Bind(R.id.connectstate)
    ConnectStateView connectStateView;

    private String Tag = "ChatListFragment";
    private Activity activity;
    private View view;

    private ChatListAdapter chatFragmentAdapter;

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

    public static ChatListFragment startFragment() {
        ChatListFragment chatListFragment = new ChatListFragment();
        return chatListFragment;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MsgFragmReceiver receiver) {
        LogManager.getLogger().d(Tag, "onEventMainThread()");
        switch (receiver.fragRecType) {
            case ALL:
                loadRooms();
                break;
            case ROOM:
                reCountUnread();
                break;

        }
    }

    /**
     * Query chat message list
     */
    protected void loadRooms() {
        new AsyncTask<Void, Void, List<RoomAttrBean>>() {

            @Override
            protected List<RoomAttrBean> doInBackground(Void... params) {
                return ConversionHelper.getInstance().loadRoomEnitites();
            }

            @Override
            protected void onPostExecute(List<RoomAttrBean> entities) {
                super.onPostExecute(entities);
                chatFragmentAdapter.setData(entities);
                reCountUnread();
            }
        }.execute();
    }

    /**
     * The calculation of article unread messages Open when the message don't disturb,
     * main interface did not read the article number is 0
     */
    protected void reCountUnread() {
        List<RoomAttrBean> entities = chatFragmentAdapter.getRoomAttrBeanList();
        int unread = 0;
        for (RoomAttrBean attr : entities) {
            unread += attr.getDisturb() == 1 ? 0 : attr.getUnread();
        }
        ((HomeActivity) activity).setFragmentDot(0, unread);
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
        chatFragmentAdapter = new ChatListAdapter(activity, recyclerFragmentChat);
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
