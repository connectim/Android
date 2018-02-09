package connect.widget.bottominput.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import connect.activity.base.BaseApplication;
import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.bean.RoomSession;
import connect.activity.chat.view.ChatEditText;
import connect.ui.activity.R;
import connect.utils.RegularUtil;
import connect.utils.data.ResourceUtil;
import connect.utils.system.SystemUtil;
import connect.widget.bottominput.InputPanel;
import connect.widget.bottominput.bean.StickerCategory;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/27.
 */
public class InputBottomLayout extends LinearLayout {

    ImageView inputmore;
    ChatEditText inputedit;
    ImageView inputface;
    ImageView inputvoice;
    TextView inputtxt;

    private static String TAG = "_InputBottomLayout";
    public static InputBottomLayout bottomLayout;

    private InputKeyListener keyListener = new InputKeyListener();
    private InputBottomEditTouch editTouch = new InputBottomEditTouch();
    private InputTextWatcher textWatcher = new InputTextWatcher();
    private InputBottomFocusChange focusChange = new InputBottomFocusChange();
    private InputBottomVoiceTouch voiceTouch = new InputBottomVoiceTouch();

    public InputBottomLayout(Context context) {
        super(context);
        initView();
    }

    public InputBottomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void initView() {
        bottomLayout = this;
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_bottom_chatinput, this);
        inputmore = (ImageView) view.findViewById(R.id.inputmore);
        inputedit = (ChatEditText) view.findViewById(R.id.inputedit);
        inputface = (ImageView) view.findViewById(R.id.inputface);
        inputvoice = (ImageView) view.findViewById(R.id.inputvoice);
        inputtxt = (TextView) view.findViewById(R.id.inputtxt);

        inputedit.requestFocus();
        inputedit.setFocusableInTouchMode(true);
        inputedit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        inputedit.setOnKeyListener(keyListener);
        inputedit.setOnTouchListener(editTouch);
        inputvoice.setOnTouchListener(voiceTouch);
        inputedit.setOnFocusChangeListener(focusChange);
        inputedit.addTextChangedListener(textWatcher);

        inputmore.setOnClickListener(clickListener);
        inputface.setOnClickListener(clickListener);
        inputtxt.setOnClickListener(clickListener);
    }

    private class InputKeyListener implements View.OnKeyListener {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            return keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN && inputedit.removeSpanString();
        }
    }

    private class InputBottomEditTouch implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ExBottomLayout.exBottomLayout.hideExView();

                Context context = BaseApplication.getInstance().getBaseContext();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(inputedit, 0);
                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.SCROLLBOTTOM);
            }
            return false;
        }
    }

    private class InputBottomVoiceTouch implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!((ChatActivity) getContext()).isOpenRecord()) {
                return true;
            }

            if (SystemUtil.isSoftShowing(InputPanel.inputPanel.getActivity())) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
            bottomLayout.setVisibility(View.INVISIBLE);
            InputPanel.inputPanel.getRecordView().setVisibility(View.VISIBLE);

            int[] location = new int[2];
            inputvoice.getLocationOnScreen(location);
            InputPanel.inputPanel.getRecordView().slideVRecord(event, location);
            if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
                bottomLayout.setVisibility(View.VISIBLE);
                InputPanel.inputPanel.getRecordView().setVisibility(View.GONE);
            }
            return true;
        }
    }

    private class InputBottomFocusChange implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            checkSendState();
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.inputmore:
                    InputPanel.inputPanel.switchMoreView();

                    RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.SCROLLBOTTOM);
                    break;
                case R.id.inputface:
                    InputPanel.inputPanel.switchEmojView();

                    inputedit.requestFocus();
                    inputedit.setFocusableInTouchMode(true);
                    break;
                case R.id.inputtxt:
                    String string = inputedit.getText().toString();
                    if (RegularUtil.matches(string, RegularUtil.OUTER_BITWEBSITE)) {
                        String title = "";
                        String sub = "";

                        if (RegularUtil.matches(string, RegularUtil.OUTER_BITWEBSITE_TRANSFER)) {//External transfer
                            title = getContext().getString(R.string.Wallet_Send_a_bitcoin_transfer_link);
                            sub = getContext().getString(R.string.Wallet_Click_to_recive_payment);
                        } else if (RegularUtil.matches(string, RegularUtil.OUTER_BITWEBSITE_PACKET)) {//inner lucky packet
                            title = getContext().getString(R.string.Wallet_Sent_via_link_luck_packet);
                            sub = getContext().getString(R.string.Chat_Send_a_Luck_Packet_Click_to_view);
                        } else if (RegularUtil.matches(string, RegularUtil.OUTER_BITWEBSITE_PAY)) {//external gather
                            title = getContext().getString(R.string.Wallet_Send_the_payment_connection);
                            sub = getContext().getString(R.string.Wallet_Click_to_transfer_bitcoin);
                        }
                        MsgSend.sendOuterMsg(MsgSend.MsgSendType.OUTER_WEBSITE, string, title, sub, "");
                    } else {
                        MsgSend.sendOuterMsg(MsgSend.MsgSendType.Text, inputedit.getText().toString(), inputedit.groupAts());
                    }
                    inputedit.setText("");
                    break;
            }
        }
    };

    private class InputTextWatcher implements TextWatcher {

        private int start;
        private int count;

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            this.start = start;
            this.count = count;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            replaceEmoticons(s, start, count);
            checkSendState();

            String string = s.toString();
            boolean isGroupChat = RoomSession.getInstance().getRoomType() == Connect.ChatType.GROUPCHAT || RoomSession.getInstance().getRoomType() == Connect.ChatType.GROUP_DISCUSSION;
            if (isGroupChat && count == 1 && "@".equals(string.substring(start))) {
                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.GROUPAT_TO);
            }
        }

        void replaceEmoticons(Editable editable, int start, int count) {
            if (count <= 0 || editable.length() < start + count)
                return;

            CharSequence s = editable.subSequence(start, start + count);
            Matcher matcher = Pattern.compile("\\[[^\\[]+\\]").matcher(s);
            while (matcher.find()) {
                int from = start + matcher.start();
                int to = start + matcher.end();
                String emot = editable.subSequence(from, to).toString();
                emot = emot.substring(1, emot.length() - 1) + ".png";
                String key = StickerCategory.emojiMaps.get(emot);
                if (!TextUtils.isEmpty(key)) {
                    emot = key;
                }

                Drawable d = ResourceUtil.getEmotDrawable(emot);
                if (d != null) {
                    ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
                    editable.setSpan(span, from, to, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }

    public void checkSendState() {
        String textMessage = inputedit.getText().toString();
        if (TextUtils.isEmpty(textMessage)||TextUtils.isEmpty(textMessage.trim())) {
            inputvoice.setVisibility(View.VISIBLE);
            inputtxt.setVisibility(View.GONE);
        } else {
            inputvoice.setVisibility(View.GONE);
            inputtxt.setVisibility(View.VISIBLE);
        }
    }

    public ChatEditText getInputedit() {
        return inputedit;
    }

    public void insertDraft(String draft) {
        int start = inputedit.getSelectionStart();
        int end = inputedit.getSelectionEnd();
        start = (start < 0 ? 0 : start);
        end = (start < 0 ? 0 : end);
        inputedit.getText().replace(start, end, draft);
    }

    public String getDraft() {
        return inputedit.getText().toString();
    }

    public void hideInputBottomEditeText() {
        Context context = getContext();
        SystemUtil.hideKeyBoard(context, inputedit);
    }
}
