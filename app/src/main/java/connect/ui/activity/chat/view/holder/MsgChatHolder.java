package connect.ui.activity.chat.view.holder;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.ui.activity.chat.BaseChatActvity;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.bean.MsgDirect;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.bean.MsgSender;
import connect.ui.activity.chat.bean.RecExtBean;
import connect.ui.activity.chat.bean.RoomSession;
import connect.ui.activity.chat.bean.RoomType;
import connect.ui.activity.chat.model.ChatMsgUtil;
import connect.ui.activity.chat.model.content.FriendChat;
import connect.ui.activity.chat.model.content.GroupChat;
import connect.ui.activity.chat.model.content.NormalChat;
import connect.ui.activity.chat.view.BurnProBar;
import connect.ui.activity.chat.view.MsgStateView;
import connect.ui.activity.contact.FriendInfoActivity;
import connect.ui.activity.contact.StrangerInfoActivity;
import connect.ui.activity.contact.bean.SourceType;
import connect.utils.TimeUtil;
import connect.utils.ToastEUtil;
import connect.utils.glide.GlideUtil;
import connect.view.ChatHeadImg;
import connect.view.prompt.ChatPromptViewManager;
import connect.view.prompt.PromptViewHelper;

/**
 * Created by gtq on 2016/11/23.
 */
public abstract class MsgChatHolder extends MsgBaseHolder {

    protected MsgDirect direct;
    protected MsgEntity baseEntity;
    protected MsgDefinBean definBean;

    protected ChatHeadImg headImg;
    protected TextView memberTxt;
    protected BurnProBar burnProBar;
    protected MsgStateView msgStateView;
    protected RelativeLayout contentLayout;
    protected MsgSender sender;

    private RoomType roomType;
    private PromptViewHelper pvHelper = null;
    protected PromptViewHelper.OnPromptClickListener promptClickListener = null;

    @TargetApi(Build.VERSION_CODES.M)
    public MsgChatHolder(View itemView) {
        super(itemView);
        headImg = (ChatHeadImg) itemView.findViewById(R.id.roundimg_head);
        memberTxt = (TextView) itemView.findViewById(R.id.usernameText);
        burnProBar = (BurnProBar) itemView.findViewById(R.id.burnprogressbar);
        msgStateView = (MsgStateView) itemView.findViewById(R.id.msgstateview);
        contentLayout = (RelativeLayout) itemView.findViewById(R.id.content_layout);

        promptClickListener = new PromptViewHelper.OnPromptClickListener() {
            @Override
            public void onPromptClick(String string) {
                String[] strings = context.getResources().getStringArray(R.array.prompt_all);
                if (string.equals(strings[0])) {//forwarding
                    transPondTo();
                } else if (string.equals(strings[1])) {//save in phone
                    saveInPhone();
                } else if (string.equals(strings[2])) {//delete
                    deleteChatMsg();
                } else if (string.equals(strings[3])) {//copy
                    ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setText(definBean.getContent());
                    ToastEUtil.makeText(context,R.string.Set_Copied).show();
                }
            }
        };
        final String[] strings = longPressPrompt();
        pvHelper = new PromptViewHelper(context);
        pvHelper.setPromptViewManager(new ChatPromptViewManager(context, strings));
        pvHelper.addPrompt(contentLayout);
        pvHelper.setOnItemClickListener(promptClickListener);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final MsgEntity entity) {
        super.buildRowData(msgBaseHolder, entity);
        try {
            baseEntity = entity;
            definBean = entity.getMsgDefinBean();
            sender = definBean.getSenderInfoExt();
            direct = ChatMsgUtil.parseMsgDirect(definBean);

            if (direct == MsgDirect.To && msgStateView != null) {
                msgStateView.setMsgEntity(entity);
                msgStateView.updateMsgState(entity.getSendstate());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        roomType = entity.getRoomType();
        switch (roomType) {
            case FriendType:
            case GroupType:
                if (RoomSession.getInstance().getBurntime() == 0) {
                    headImg.setVisibility(View.VISIBLE);
                } else {
                    headImg.setVisibility(View.GONE);
                }
                headImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (direct == MsgDirect.From) {
                            String pubkey = sender.getPublickey();
                            ContactEntity friend = ContactHelper.getInstance().loadFriendEntity(pubkey);
                            if (friend == null) {
                                StrangerInfoActivity.startActivity((Activity) context, sender.address, SourceType.GROUP);
                            } else {
                                FriendInfoActivity.startActivity((Activity) context, sender.getPublickey());
                            }
                        }
                    }
                });

                if (direct == MsgDirect.From) {
                    if (roomType == RoomType.FriendType) {
                        memberTxt.setVisibility(View.GONE);

                        String friendAvatar = (((BaseChatActvity) context).getBaseChat()).headImg();
                        GlideUtil.loadAvater(headImg, friendAvatar);
                    } else {
                        memberTxt.setVisibility(View.VISIBLE);
                        String showName = ((GroupChat) ((BaseChatActvity) context).getBaseChat()).nickName(sender.getPublickey());
                        if (TextUtils.isEmpty(showName)) {
                            showName = sender.username;
                        }
                        memberTxt.setText(showName);

                        GlideUtil.loadAvater(headImg, sender.avatar);
                    }
                } else {
                    GlideUtil.loadAvater(headImg, sender.avatar);
                }

                if (burnProBar != null) {
                    burnProBar.initBurnMsg(direct, entity);
                }
                break;
            case RobotType:
                if (burnProBar != null) {
                    burnProBar.setVisibility(View.GONE);
                }
                if (direct == MsgDirect.From) {
                    GlideUtil.loadImage(headImg, R.mipmap.connect_logo);
                    if (memberTxt != null) {
                        memberTxt.setVisibility(View.GONE);
                    }
                } else {
                    GlideUtil.loadAvater(headImg, sender.avatar);
                }
                break;
        }
    }

    public void deleteChatMsg() {
        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.DELMSG, baseEntity);
        MessageHelper.getInstance().deleteMsgByid(baseEntity.getMsgDefinBean().getMessage_id());
    }

    public void saveInPhone() {

    }

    public void transPondTo() {

    }
}
