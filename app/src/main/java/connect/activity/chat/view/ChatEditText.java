package connect.activity.chat.view;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.EditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connect.database.green.bean.GroupMemberEntity;
import connect.activity.chat.bean.RecExtBean;
import connect.utils.RegularUtil;

/**
 * Created by pujin on 2017/3/13.
 */

public class ChatEditText extends EditText{

    private Map<String, GroupMemberEntity> atMemberMap = new HashMap<>();

    public ChatEditText(Context context) {
        super(context);
    }

    public ChatEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RecExtBean bean) {
        Object[] objects = null;
        if (bean.getObj() != null) {
            objects = (Object[]) bean.getObj();
        }
        switch (bean.getExtType()) {
            case GROUP_AT:
                GroupMemberEntity memEntity = (GroupMemberEntity) objects[0];
                atMemberMap.put(memEntity.getUid(), memEntity);

                int start = getSelectionStart();
                getText().delete(start - 1, 1);

                String showName = memEntity.getUsername();
                showName = "@" + showName + " ";
                SpannableStringBuilder builder = new SpannableStringBuilder(showName);
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.BLACK);
                builder.setSpan(colorSpan, 0, showName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                append(builder);
                break;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    public List<String> groupAts() {
        String contents = getText().toString();

        List<String> addressList = new ArrayList<>();
        for (Map.Entry<String, GroupMemberEntity> entry : atMemberMap.entrySet()) {
            GroupMemberEntity memEntity = entry.getValue();
            String showName = memEntity.getUsername();
            showName = "@" + showName + " ";

            if (contents.contains(showName)) {
                addressList.add(memEntity.getUid());
            }
        }
        return addressList;
    }

    public boolean removeSpanString() {
        boolean removeAt = false;
        int startRemove = 0;
        int startSelect = getSelectionStart();
        String contents = getText().toString();

        String firstHalf = contents.substring(0, startSelect);
        if (RegularUtil.matches(firstHalf, "@.* ")) {
            for (Map.Entry<String, GroupMemberEntity> entry : atMemberMap.entrySet()) {
                GroupMemberEntity memEntity = entry.getValue();
                String showName = memEntity.getUsername();
                showName = "@" + showName + " ";
                int lastAtSelect = contents.lastIndexOf("@");
                if (firstHalf.substring(lastAtSelect).contains(showName)) {
                    startRemove = lastAtSelect;
                    removeAt = true;
                    break;
                }
            }
        }
        if (removeAt) {
            getText().delete(startRemove, startSelect);
        }
        return removeAt;
    }
}
