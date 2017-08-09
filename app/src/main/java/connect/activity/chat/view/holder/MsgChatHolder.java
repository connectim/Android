package connect.activity.chat.view.holder;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.activity.chat.BaseChatActvity;
import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.MsgSender;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.bean.RoomSession;
import connect.activity.chat.model.ChatMsgUtil;
import connect.activity.chat.model.content.GroupChat;
import connect.activity.chat.view.BurnProBar;
import connect.activity.chat.view.MsgStateView;
import connect.activity.contact.FriendInfoActivity;
import connect.activity.contact.StrangerInfoActivity;
import connect.activity.contact.bean.SourceType;
import connect.utils.TimeUtil;
import connect.utils.ToastEUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.ChatHeadImg;
import connect.widget.prompt.ChatPromptViewManager;
import connect.widget.prompt.PromptViewHelper;

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

        final String[] strings = longPressPrompt();
        pvHelper = new PromptViewHelper(context);
        pvHelper.setPromptViewManager(new ChatPromptViewManager(context, strings));
        pvHelper.addPrompt(longClickView());
        pvHelper.setOnItemClickListener(promptClickListener);

        switch (RoomSession.getInstance().getRoomType()) {
            case 0:
            case 1:
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
                    if (RoomSession.getInstance().getRoomType() == 0 || RoomSession.getInstance().getRoomType() == 2) {
                        memberTxt.setVisibility(View.GONE);
                    } else {
                        memberTxt.setVisibility(View.VISIBLE);

                        String showName = ((GroupChat) ((BaseChatActvity) context).getBaseChat()).nickName(sender.getPublickey());
                        if (TextUtils.isEmpty(showName)) {
                            showName = sender.username;
                        }
                        memberTxt.setText(showName);
                    }
                }

                if (direct == MsgDirect.From && RoomSession.getInstance().getRoomType() == 0) {
                    GlideUtil.loadAvater(headImg, RoomSession.getInstance().getFriendAvatar());
                } else if (direct == MsgDirect.To) {
                    GlideUtil.loadAvater(headImg, MemoryDataManager.getInstance().getAvatar());
                } else if (sender != null) {
                    GlideUtil.loadAvater(headImg, sender.avatar);
                }

                if (burnProBar != null) {
                    if (TextUtils.isEmpty(definBean.getExt())) {
                        burnProBar.setVisibility(View.GONE);
                    } else {
                        if (direct == MsgDirect.From || entity.getSendstate() == 1) {
                            burnProBar.setVisibility(View.VISIBLE);
                        } else {
                            burnProBar.setVisibility(View.GONE);
                        }
                        burnProBar.initBurnMsg((MsgEntity) entity);
                        if (direct == MsgDirect.From && (entity.getMsgDefinBean().getType() == 1 || entity.getMsgDefinBean().getType() == 5)) {
                            ((MsgEntity) entity).setBurnstarttime(TimeUtil.getCurrentTimeInLong());
                            burnProBar.startBurnRead();
                            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.BURNMSG_READ, entity.getMsgDefinBean().getMessage_id(), direct);
                        }
                    }
                }
                break;
            case 2:
                if (burnProBar != null) {
                    burnProBar.setVisibility(View.GONE);
                }
                if (direct == MsgDirect.From) {
                    GlideUtil.loadImage(headImg, R.mipmap.connect_logo);
                    if (memberTxt != null) {
                        memberTxt.setVisibility(View.GONE);
                    }
                } else {
                    String imgpath = MemoryDataManager.getInstance().getAvatar();
                    GlideUtil.loadAvater(headImg, imgpath);
                }
                break;
        }
    }

    public void deleteChatMsg() {
        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.DELMSG, baseEntity);
        MessageHelper.getInstance().deleteMsgByid(baseEntity.getMsgDefinBean().getMessage_id());
    }

    public void saveInPhone() {

    }

    public void transPondTo() {

    }

    public View longClickView() {
        return contentLayout;
    }
}
