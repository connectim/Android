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
import connect.activity.chat.BaseChatActvity;
import connect.activity.chat.bean.DestructReadBean;
import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.bean.RoomSession;
import connect.activity.chat.model.content.GroupChat;
import connect.activity.chat.view.BurnProBar;
import connect.activity.chat.view.MsgStateView;
import connect.activity.contact.FriendInfoActivity;
import connect.activity.contact.StrangerInfoActivity;
import connect.activity.contact.bean.SourceType;
import connect.activity.set.UserInfoActivity;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.utils.TimeUtil;
import connect.utils.ToastEUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.glide.GlideUtil;
import connect.widget.ChatHeadImg;
import connect.widget.prompt.ChatPromptViewManager;
import connect.widget.prompt.PromptViewHelper;
import protos.Connect;

/**
 * Created by gtq on 2016/11/23.
 */
public abstract class MsgChatHolder extends MsgBaseHolder {

    protected ChatHeadImg headImg;
    protected TextView memberTxt;
    protected BurnProBar burnProBar;
    protected MsgStateView msgStateView;
    protected RelativeLayout contentLayout;

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
                    cm.setText(getCopyTxt());
                    ToastEUtil.makeText(context, R.string.Set_Copied).show();
                }
            }
        };
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final MsgExtEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        final MsgDirect direct = msgExtEntity.parseDirect();
        try {
            if (direct == MsgDirect.To && msgStateView != null) {
                msgStateView.setMsgEntity(msgExtEntity);
                msgStateView.updateMsgState(msgExtEntity.getSend_status());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String[] strings = longPressPrompt();
        pvHelper = new PromptViewHelper(context);
        pvHelper.setPromptViewManager(new ChatPromptViewManager(context, strings));
        pvHelper.addPrompt(longClickView());
        pvHelper.setOnItemClickListener(promptClickListener);

        Connect.ChatType chatType = Connect.ChatType.forNumber(msgExtEntity.getChatType());
        switch (chatType) {
            case PRIVATE:
                final Connect.MessageUserInfo userInfo = RoomSession.getInstance().getUserInfo();
                GlideUtil.loadAvater(headImg, direct == MsgDirect.From ? userInfo.getAvatar() :
                        MemoryDataManager.getInstance().getAvatar());
                headImg.setVisibility(RoomSession.getInstance().getBurntime() <= 0 ? View.VISIBLE :
                        View.GONE);
                headImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (direct == MsgDirect.To) {
                            UserInfoActivity.startActivity((Activity) context);
                        } else if (direct == MsgDirect.From) {
                            ContactEntity friend = ContactHelper.getInstance().loadFriendEntity(userInfo.getUid());
                            if (friend == null) {
                                String address = SupportKeyUril.getAddressFromPubkey(userInfo.getUid());
                                StrangerInfoActivity.startActivity((Activity) context, address, SourceType.GROUP);
                            } else {
                                FriendInfoActivity.startActivity((Activity) context, userInfo.getUid());
                            }
                        }
                    }
                });
                if (memberTxt != null) {
                    memberTxt.setVisibility(View.GONE);
                }
                if (burnProBar != null) {
                    long destructtime = msgExtEntity.parseDestructTime();
                    if (destructtime == 0) {
                        burnProBar.setVisibility(View.GONE);
                    } else {
                        burnProBar.setVisibility(View.VISIBLE);
                        burnProBar.setMsgExtEntity(msgExtEntity);

                        MsgType msgType = MsgType.toMsgType(msgExtEntity.getMessageType());
                        burnProBar.loadBurnMsg();
                        if (direct == MsgDirect.From && (msgType == MsgType.Text || msgType == MsgType.Emotion)) {
                            msgExtEntity.setSnap_time(TimeUtil.getCurrentTimeInLong());
                            DestructReadBean.getInstance().sendEventDelay(msgExtEntity.getMessage_id());
                        }
                    }
                }
                break;
            case GROUPCHAT:
                headImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (direct == MsgDirect.To) {
                            UserInfoActivity.startActivity((Activity) context);
                        } else if (direct == MsgDirect.From) {
                            String memberKey=msgExtEntity.getMessage_from();
                            ContactEntity friend = ContactHelper.getInstance().loadFriendEntity(memberKey);
                            if (friend == null) {
                                String address = SupportKeyUril.getAddressFromPubkey(memberKey);
                                StrangerInfoActivity.startActivity((Activity) context, address, SourceType.GROUP);
                            } else {
                                FriendInfoActivity.startActivity((Activity) context, memberKey);
                            }
                        }
                    }
                });

                if (direct == MsgDirect.To) {
                    GlideUtil.loadAvater(headImg, MemoryDataManager.getInstance().getAvatar());
                    if (memberTxt != null) {
                        memberTxt.setVisibility(View.GONE);
                    }
                } else if (direct == MsgDirect.From) {
                    memberTxt.setVisibility(View.VISIBLE);
                    String memberKey = msgExtEntity.getMessage_from();
                    GroupMemberEntity memberEntity = ((GroupChat) ((BaseChatActvity) context).getNormalChat()).loadGroupMember(memberKey);

                    GlideUtil.loadAvater(headImg, memberEntity.getAvatar());

                    String memberName = "";
                    if (memberEntity != null) {
                        memberName = TextUtils.isEmpty(memberEntity.getNick()) ? memberEntity.getUsername() : memberEntity.getNick();
                    }
                    memberTxt.setText(memberName);
                }

                if (burnProBar != null) {
                    burnProBar.setVisibility(View.GONE);
                }
                break;
            case CONNECT_SYSTEM:
                if (burnProBar != null) {
                    burnProBar.setVisibility(View.GONE);
                }
                if (memberTxt != null) {
                    memberTxt.setVisibility(View.GONE);
                }
                if (direct == MsgDirect.From) {
                    GlideUtil.loadImage(headImg, R.mipmap.connect_logo);
                } else {
                    String imgpath = MemoryDataManager.getInstance().getAvatar();
                    GlideUtil.loadAvater(headImg, imgpath);
                }
                break;
        }
    }

    public void deleteChatMsg() {
        MsgExtEntity msgExtEntity = getMsgExtEntity();
        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.DELMSG, msgExtEntity);
        MessageHelper.getInstance().deleteMsgByid(msgExtEntity.getMessage_id());
    }

    public void saveInPhone() {

    }

    public void transPondTo() {

    }

    public View longClickView() {
        return contentLayout;
    }

    public String getCopyTxt() {
        return "";
    }
}
