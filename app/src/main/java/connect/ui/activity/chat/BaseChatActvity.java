package connect.ui.activity.chat;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.text.TextUtils;
import android.view.KeyEvent;

import connect.db.green.DaoHelper.ConversionHelper;
import connect.db.green.bean.ConversionEntity;
import connect.im.msgdeal.SendMsgUtil;
import connect.ui.activity.chat.bean.RoomSession;
import connect.ui.activity.chat.bean.Talker;
import connect.ui.activity.chat.model.InputPanel;
import connect.ui.activity.chat.model.content.BaseChat;
import connect.ui.activity.chat.model.content.FriendChat;
import connect.ui.activity.chat.model.content.GroupChat;
import connect.ui.activity.chat.model.content.RobotChat;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.view.RecycleViewScrollHelper;

/**
 * Created by pujin on 2017/1/19.
 */
public abstract class BaseChatActvity extends BaseActivity {

    protected static String ROOM_TALKER = "ROOM_TALKER";

    protected Talker talker;
    protected RoomSession roomSession;
    protected ConversionEntity roomEntity;
    protected BaseChat baseChat;
    protected RecycleViewScrollHelper scrollHelper;
    protected InputPanel inputPanel = null;
    protected LinearLayoutManager linearLayoutManager;

    @Override
    public void initView() {
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE) ;
        mNotificationManager.cancel(1001);

        talker= (Talker) getIntent().getSerializableExtra(ROOM_TALKER);
        roomSession = RoomSession.getInstance();
        roomSession.setRoomType(talker.getTalkType());
        roomSession.setRoomKey(talker.getTalkKey());

        switch (talker.getTalkType()) {
            case 0:
                roomSession.setRoomName(talker.getTalkName());
                roomSession.setFriendAvatar(talker.getFriendEntity().getAvatar());
                baseChat = new FriendChat(talker.getFriendEntity());

                if (!TextUtils.isEmpty(baseChat.address())) {
                    SendMsgUtil.friendChatCookie(baseChat.roomKey());
                }
                break;
            case 1:
                roomSession.setRoomName(talker.getTalkName());
                roomSession.setGroupEcdh(talker.getGroupEntity().getEcdh_key());
                baseChat = new GroupChat(talker.getGroupEntity());
                break;
            case 2:
                baseChat = RobotChat.getInstance();
                roomSession.setRoomName(baseChat.nickName());
                break;
        }

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        inputPanel = new InputPanel(getWindow().getDecorView().findViewById(android.R.id.content));
        inputPanel.isGroupAt(talker.getTalkType() == 1);
        scrollHelper = new RecycleViewScrollHelper(new RecycleViewScrollHelper.OnScrollPositionChangedListener() {

            @Override
            public void onScrollToTop() {
                loadMoreMsgs();
            }

            @Override
            public void onScrollToBottom() {

            }

            @Override
            public void onScrollToUnknown(boolean isTopViewVisible, boolean isBottomViewVisible) {

            }
        });
        scrollHelper.setCheckIfItemViewFullRecycleViewForTop(true);

        roomEntity = ConversionHelper.getInstance().loadRoomEnitity(talker.getTalkKey());
        if (roomEntity != null) {
            if (!TextUtils.isEmpty(roomEntity.getDraft())) {
                inputPanel.insertDraft(" " + roomEntity.getDraft());
            }
            ConversionHelper.getInstance().updateRoomEntity(roomEntity);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            saveRoomInfo();
            ActivityUtil.goBack(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /** first start ,load message */
    abstract void loadChatInfor();

    /** load more */
    abstract void loadMoreMsgs();

    /** update chat list */
    abstract void saveRoomInfo();

    public BaseChat getBaseChat() {
        return baseChat;
    }
}
