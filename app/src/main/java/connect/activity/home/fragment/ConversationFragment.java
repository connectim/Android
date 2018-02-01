package connect.activity.home.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseFragment;
import connect.activity.chat.SearchActivity;
import connect.activity.home.HomeActivity;
import connect.activity.home.adapter.ConversationAdapter;
import connect.activity.home.bean.ConversationAction;
import connect.activity.home.bean.RoomAttrBean;
import connect.activity.home.fragment.view.ChatAddPopWindow;
import connect.activity.home.view.ConnectStateView;
import connect.activity.home.view.LineDecoration;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
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
        switch (action.getConverType()) {
            case LOAD_MESSAGE:
                loadRooms();
                break;
            case LOAD_UNREAD:
                countUnread();
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
                return ConversionHelper.getInstance().loadRoomEntites();
            }

            @Override
            protected void onPostExecute(List<RoomAttrBean> entities) {
                super.onPostExecute(entities);
                chatFragmentAdapter.setData(entities);
                countUnread();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//并行执行任务
    }

    protected void countUnread() {
        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... params) {
                return ConversionHelper.getInstance().countUnReads();
            }

            @Override
            protected void onPostExecute(Integer integer) {
                ((HomeActivity) activity).setFragmentDot(0, integer);
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
        chatFragmentAdapter = new ConversationAdapter(recyclerFragmentChat);
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

    @OnClick({R.id.relativelayout_1, R.id.search_image1})
    void onClickListener(View view) {
        switch (view.getId()) {
            case R.id.relativelayout_1:
                PopupWindow popWindow = new ChatAddPopWindow(getActivity());
                popWindow.showAsDropDown(connectStateView.findViewById(R.id.relativelayout_1), 5, 5);
                break;
            case R.id.search_image1:
                ActivityUtil.next(activity, SearchActivity.class);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogManager.getLogger().d(Tag, "onDestroyView()");
        ButterKnife.unbind(this);
        EventBus.getDefault().unregister(this);
    }
}
