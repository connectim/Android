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

import connect.activity.base.BaseListener;
import connect.activity.chat.bean.DestructReadBean;
import connect.activity.chat.bean.LinkMessageRow;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.bean.RoomSession;
import connect.activity.chat.model.GroupMemberUtil;
import connect.activity.chat.view.BurnProBar;
import connect.activity.chat.view.MsgStateView;
import connect.activity.contact.FriendInfoActivity;
import connect.activity.contact.StrangerInfoActivity;
import connect.activity.contact.bean.SourceType;
import connect.activity.set.UserInfoActivity;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.utils.TimeUtil;
import connect.utils.ToastEUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.ChatHeadImg;
import connect.widget.prompt.ChatPromptViewManager;
import connect.widget.prompt.PromptViewHelper;
import instant.bean.ChatMsgEntity;
import instant.bean.MsgDirect;
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
    public void buildRowData(MsgBaseHolder msgBaseHolder, final ChatMsgEntity msgExtEntity) throws Exception {
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
                GlideUtil.loadAvatarRound(headImg, direct == MsgDirect.From ?
                        RoomSession.getInstance().getChatAvatar() :
                        SharedPreferenceUtil.getInstance().getUser().getAvatar());

                headImg.setVisibility(RoomSession.getInstance().getBurntime() <= 0 ? View.VISIBLE :
                        View.GONE);
                headImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (direct == MsgDirect.To) {
                            UserInfoActivity.startActivity((Activity) context);
                        } else if (direct == MsgDirect.From) {
                            String uid = RoomSession.getInstance().getRoomKey();
                            ContactEntity friend = ContactHelper.getInstance().loadFriendEntity(uid);
                            if (friend == null) {
                                //String address = SupportKeyUril.getAddressFromPubKey(uid);
                                String address = "";
                                StrangerInfoActivity.startActivity((Activity) context, address, SourceType.GROUP);
                            } else {
                                FriendInfoActivity.startActivity((Activity) context, uid);
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

                        LinkMessageRow msgType = LinkMessageRow.toMsgType(msgExtEntity.getMessageType());
                        burnProBar.loadBurnMsg();
                        if (direct == MsgDirect.From && (msgType == LinkMessageRow.Text || msgType == LinkMessageRow.Emotion)) {
                            msgExtEntity.setSnap_time(TimeUtil.getCurrentTimeInLong());
                            DestructReadBean.getInstance().sendEventDelay(msgExtEntity.getMessage_id());
                        }
                    }
                }
                break;
            case GROUPCHAT:
            case GROUP_DISCUSSION:
                headImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (direct == MsgDirect.To) {
                            UserInfoActivity.startActivity((Activity) context);
                        } else if (direct == MsgDirect.From) {
                            String memberKey = msgExtEntity.getMessage_from();
                            ContactEntity friend = ContactHelper.getInstance().loadFriendEntity(memberKey);
                            if (friend == null) {
                                StrangerInfoActivity.startActivity((Activity) context, memberKey, SourceType.GROUP);
                            } else {
                                FriendInfoActivity.startActivity((Activity) context, memberKey);
                            }
                        }
                    }
                });

                if (direct == MsgDirect.To) {
                    GlideUtil.loadAvatarRound(headImg, SharedPreferenceUtil.getInstance().getUser().getAvatar());
                    if (memberTxt != null) {
                        memberTxt.setVisibility(View.GONE);
                    }
                } else if (direct == MsgDirect.From) {
                    memberTxt.setVisibility(View.VISIBLE);
                    String groupKey = msgExtEntity.getMessage_ower();
                    String memberKey = msgExtEntity.getMessage_from();

                    GroupMemberUtil.groupMemberUtil.loadGroupMember(groupKey, memberKey, new BaseListener<GroupMemberEntity>() {
                        @Override
                        public void Success(GroupMemberEntity ts) {
                            GlideUtil.loadAvatarRound(headImg, ts.getAvatar());

                            String memberName = "";
                            if (ts != null) {
                                memberName = TextUtils.isEmpty(ts.getNick()) ? ts.getUsername() : ts.getNick();
                            }
                            memberTxt.setText(memberName);
                        }

                        @Override
                        public void fail(Object... objects) {
                            GlideUtil.loadAvatarRound(headImg, "");
                            memberTxt.setText("");
                        }
                    });
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
                    GlideUtil.loadAvatarRound(headImg, R.mipmap.connect_logo);
                } else {
                    String imgpath = SharedPreferenceUtil.getInstance().getUser().getAvatar();
                    GlideUtil.loadAvatarRound(headImg, imgpath);
                }
                break;
        }
    }

    public void deleteChatMsg() {
        ChatMsgEntity msgExtEntity = getMsgExtEntity();
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
