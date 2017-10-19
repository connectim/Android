package connect.activity.chat.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.set.contract.SingleSetContract;
import connect.activity.chat.set.presenter.SingleSetPresenter;
import connect.activity.contact.FriendInfoActivity;
import connect.activity.contact.StrangerInfoActivity;
import connect.activity.contact.bean.SourceType;
import connect.activity.home.bean.ConversationAction;
import connect.activity.set.UserInfoActivity;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.ConversionSettingHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ConversionEntity;
import connect.database.green.bean.ConversionSettingEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.widget.TopToolBar;

/**
 * private chat setting
 * Created by gtq on 2016/11/22.
 */
public class SingleSetActivity extends BaseActivity implements SingleSetContract.BView{

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.linearlayout)
    LinearLayout linearlayout;

    private SingleSetActivity activity;
    private static String ROOM_KEY = "ROOM_KEY";
    private String roomKey = "";

    private SingleSetContract.Presenter presenter;

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
        new SingleSetPresenter(this).start();
    }

    @Override
    public void setPresenter(SingleSetContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getRoomKey() {
        return roomKey;
    }

    @Override
    public void switchTop(String name, boolean state) {
        View view = findViewById(R.id.top);
        TextView txt = (TextView) view.findViewById(R.id.txt);
        txt.setText(name);

        View topToggle = view.findViewById(R.id.toggle);
        topToggle.setSelected(state);
        topToggle.setTag(name);
        topToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                int top = v.isSelected() ? 1 : 0;

                ConversionEntity conversionEntity = ConversionHelper.getInstance().loadRoomEnitity(roomKey);
                if (conversionEntity == null) {
                    conversionEntity = new ConversionEntity();
                    conversionEntity.setIdentifier(roomKey);
                }
                conversionEntity.setTop(top);
                ConversionHelper.getInstance().insertRoomEntity(conversionEntity);

                ConversationAction.conversationAction.sendEvent();
            }
        });
    }

    @Override
    public void switchDisturb(String name, boolean state) {
        View view = findViewById(R.id.mute);
        TextView txt = (TextView) view.findViewById(R.id.txt);
        txt.setText(name);

        View topToggle = view.findViewById(R.id.toggle);
        topToggle.setSelected(state);
        topToggle.setTag(name);
        topToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                int disturb = v.isSelected() ? 1 : 0;

                ConversionSettingEntity settingEntity = ConversionSettingHelper.getInstance().loadSetEntity(roomKey);
                if (settingEntity == null) {
                    settingEntity = new ConversionSettingEntity();
                    settingEntity.setIdentifier(roomKey);
                }
                settingEntity.setDisturb(disturb);
                ConversionSettingHelper.getInstance().insertSetEntity(settingEntity);

                ConversationAction.conversationAction.sendEvent();
            }
        });
    }

    @Override
    public void clearMessage() {
        View view = findViewById(R.id.clear);
        String str = getResources().getString(R.string.Link_Clear_Chat_History);
        TextView txt = (TextView) view.findViewById(R.id.txt);
        txt.setText(str);

        ImageView img = (ImageView) view.findViewById(R.id.img);
        img.setBackgroundResource(R.mipmap.message_clear_history2x);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
    }

    @Override
    public void showContactInfo(View view) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.linearlayout);
        layout.addView(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = (String) v.getTag();
                // qwert
                /*if (TextUtils.isEmpty(address)) {
                    GroupCreateActivity.startActivity(activity, roomKey);
                } else if (MemoryDataManager.getInstance().getAddress().equals(address)) {
                    UserInfoActivity.startActivity(activity);
                } else {
                    ContactEntity entity = ContactHelper.getInstance().loadFriendEntity(address);
                    if (entity == null) {
                        StrangerInfoActivity.startActivity(activity, address, SourceType.SEARCH);
                    } else {
                        FriendInfoActivity.startActivity(activity, entity.getPub_key());
                    }
                }*/
            }
        });
    }
}
