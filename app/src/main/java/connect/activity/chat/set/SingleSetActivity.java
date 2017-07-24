package connect.activity.chat.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.ConversionSettingHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ConversionEntity;
import connect.database.green.bean.ConversionSettingEntity;
import connect.ui.activity.R;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.contact.FriendInfoActivity;
import connect.activity.contact.StrangerInfoActivity;
import connect.activity.contact.bean.SourceType;
import connect.activity.set.ModifyInfoActivity;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;
import connect.widget.roundedimageview.RoundedImageView;

/**
 * private chat setting
 * Created by gtq on 2016/11/22.
 */
public class SingleSetActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.linearlayout)
    LinearLayout linearlayout;

    private SingleSetActivity activity;
    private static String ROOM_KEY = "ROOM_KEY";
    private String roomKey;

    private ConversionEntity roomEntity;
    private ContactEntity friendEntity;
    private ConversionSettingEntity chatSetEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singleset);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String roomkey) {
        Bundle bundle = new Bundle();
        bundle.putString(ROOM_KEY, roomkey);
        ActivityUtil.next(activity, SingleSetActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Wallet_Detail));
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        roomKey = getIntent().getStringExtra(ROOM_KEY);
        if (TextUtils.isEmpty(roomKey)) {
            ActivityUtil.goBack(activity);
            return;
        }

        roomEntity = ConversionHelper.getInstance().loadRoomEnitity(roomKey);
        chatSetEntity = ConversionSettingHelper.getInstance().loadSetEntity(roomKey);
        boolean istop = false;
        boolean isDisturb = false;
        if (roomEntity == null) {
            roomEntity = new ConversionEntity();
            roomEntity.setIdentifier(roomKey);
            roomEntity.setTop(0);
        }
        if (chatSetEntity == null) {
            chatSetEntity = new ConversionSettingEntity();
            chatSetEntity.setIdentifier(roomKey);
            chatSetEntity.setDisturb(0);
        }

        if (Integer.valueOf(1).equals(roomEntity.getTop())) {
            istop = true;
        }
        if (Integer.valueOf(1).equals(chatSetEntity.getDisturb())) {
            isDisturb = true;
        }

        seSwitchLayout(findViewById(R.id.top), getResources().getString(R.string.Chat_Sticky_on_Top_chat), istop);
        seSwitchLayout(findViewById(R.id.mute), getResources().getString(R.string.Chat_Mute_Notification), isDisturb);

        setOtherLayout(findViewById(R.id.clear), R.mipmap.message_clear_history2x, getResources().getString(R.string.Link_Clear_Chat_History));

        friendEntity = ContactHelper.getInstance().loadFriendEntity(roomKey);
        if (friendEntity == null) {
            ActivityUtil.goBack(this);
            return;
        }
        addContactInfor((LinearLayout) findViewById(R.id.linearlayout));
    }

    protected void seSwitchLayout(View view, String name, boolean state) {
        TextView txt = (TextView) view.findViewById(R.id.txt);
        txt.setText(name);

        View topToggle = view.findViewById(R.id.toggle);
        topToggle.setSelected(state);
        topToggle.setTag(name);
        topToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = (String) v.getTag();
                v.setSelected(!v.isSelected());
                if (getResources().getString(R.string.Chat_Sticky_on_Top_chat).equals(name)) {
                    int top = v.isSelected() ? 1 : 0;
                    roomEntity.setTop(top);
                    ConversionHelper.getInstance().insertRoomEntity(roomEntity);
                } else if (getResources().getString(R.string.Chat_Mute_Notification).equals(name)) {
                    int disturb = v.isSelected() ? 1 : 0;
                    chatSetEntity.setDisturb(disturb);
                    ConversionSettingHelper.getInstance().insertSetEntity(chatSetEntity);
                }
            }
        });
    }

    protected void setOtherLayout(View view, int imgid, String str) {
        TextView txt = (TextView) view.findViewById(R.id.txt);
        txt.setText(str);

        ImageView img = (ImageView) view.findViewById(R.id.img);
        img.setBackgroundResource(imgid);
        view.setTag(str);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = (String) v.getTag();
                if (getResources().getString(R.string.Link_Clear_Chat_History).equals(tag)) {
                    ArrayList<String> strings = new ArrayList();
                    strings.add(getString(R.string.Link_Clear_Chat_History));
                    DialogUtil.showBottomView(activity, strings, new DialogUtil.DialogListItemClickListener() {
                        @Override
                        public void confirm(int position) {
                            ConversionHelper.getInstance().deleteRoom(roomKey);
                            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.CLEAR_HISTORY);
                        }
                    });
                }
            }
        });
    }

    private void addContactInfor(LinearLayout layout) {
        List<ContactEntity> entities = new ArrayList<>();
        entities.add(friendEntity);
        entities.add(new ContactEntity());

        for (ContactEntity entity : entities) {
            View view = LayoutInflater.from(activity).inflate(R.layout.linear_contact, null);
            RoundedImageView headimg = (RoundedImageView) view.findViewById(R.id.roundimg);
            ImageView adminImg= (ImageView) view.findViewById(R.id.img1);
            TextView name = (TextView) view.findViewById(R.id.name);

            adminImg.setVisibility(View.GONE);
            if (TextUtils.isEmpty(entity.getUsername())) {
                name.setVisibility(View.GONE);
            } else {
                name.setVisibility(View.VISIBLE);
                String nametxt = TextUtils.isEmpty(entity.getRemark()) ? entity.getUsername() : entity.getRemark();
                name.setText(nametxt);
            }

            if (TextUtils.isEmpty(entity.getAvatar())) {
                GlideUtil.loadImage(headimg, R.mipmap.message_add_friends2x);
            } else {
                GlideUtil.loadAvater(headimg, entity.getAvatar());
            }

            view.setTag(entity.getAddress());
            layout.addView(view);
            view.setOnClickListener(contactClick);
        }
    }

    private View.OnClickListener contactClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String address = (String) v.getTag();
            if (TextUtils.isEmpty(address)) {
                ContactSelectActivity.startCreateGroupActivity(activity, roomKey);
            } else if (MemoryDataManager.getInstance().getAddress().equals(address)) {
                ModifyInfoActivity.startActivity(activity);
            } else {
                ContactEntity entity = ContactHelper.getInstance().loadFriendEntity(address);
                if (entity == null) {
                    StrangerInfoActivity.startActivity(activity, address, SourceType.SEARCH);
                } else {
                    FriendInfoActivity.startActivity(activity, entity.getPub_key());
                }
            }
        }
    };
}
