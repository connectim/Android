package connect.activity.chat.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.bean.StickerCategory;
import connect.activity.chat.bean.WebsiteExt1Bean;
import connect.activity.chat.exts.GroupAtActivity;
import connect.activity.chat.inter.IEmojiClickListener;
import connect.activity.chat.view.ChatEditText;
import connect.activity.chat.view.ExBottomLayout;
import connect.activity.chat.view.RecordView;
import connect.utils.RegularUtil;
import connect.utils.data.ResourceUtil;
import connect.utils.system.SystemUtil;

/**
 * Created by gtq on 2016/11/26.
 */
public class InputPanel {

    private Context context;
    private View inputView;
    private ExBottomLayout exBottomLayout;
    private RecordView recordView;

    private ImageView inputMore;
    private ChatEditText inputEdit;
    private ImageView inputFace;
    private ImageView inputVoice;
    private TextView inputTxt;

    private boolean isGroupAt = false;

    public InputPanel(View rootview) {
        context = rootview.getContext();
        inputView = rootview.findViewById(R.id.layout_inputbottom);
        exBottomLayout = (ExBottomLayout) rootview.findViewById(R.id.layout_exbottom);
        recordView = (RecordView) rootview.findViewById(R.id.recordview);

        inputMore = (ImageView) inputView.findViewById(R.id.inputmore);
        inputEdit = (ChatEditText) inputView.findViewById(R.id.inputedit);
        inputFace = (ImageView) inputView.findViewById(R.id.inputface);
        inputVoice = (ImageView) inputView.findViewById(R.id.inputvoice);
        inputTxt = (TextView) inputView.findViewById(R.id.inputtxt);

        recordView.setVisibility(View.GONE);
        initEdit();

        inputMore.setOnClickListener(clickListener);
        inputFace.setOnClickListener(clickListener);
        inputTxt.setOnClickListener(clickListener);
        inputVoice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if( !((ChatActivity)context).isOpenRecord()){
                    return true;
                }

                inputView.setVisibility(View.INVISIBLE);
                recordView.setVisibility(View.VISIBLE);
                int[] location = new int[2];
                inputVoice.getLocationOnScreen(location);
                recordView.slideVRecord(event, location);
                if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
                    inputView.setVisibility(View.VISIBLE);
                    recordView.setVisibility(View.GONE);
                }
                return true;
            }
        });
        exBottomLayout.getEmojiPanel().setiEmojiClickListener(new IEmojiClickListener() {
            @Override
            public void onEmjClick(String emi) {
                Editable mEditable = inputEdit.getText();

                if (emi.equals("[DEL]")) {
                    inputEdit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                } else {
                    int start = inputEdit.getSelectionStart();
                    int end = inputEdit.getSelectionEnd();
                    start = (start < 0 ? 0 : start);
                    end = (start < 0 ? 0 : end);
                    mEditable.replace(start, end, emi);
                }
            }

            @Override
            public void onEmtClick(String emt) {
                MsgSend.sendOuterMsg(MsgType.Emotion, emt);
            }
        });
    }

    protected void focusEdit() {
        inputEdit.requestFocus();
        inputEdit.setFocusableInTouchMode(true);
    }

    public void initEdit() {
        focusEdit();
        inputEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        inputEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN&&inputEdit.removeSpanString();
            }
        });
        inputEdit.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    exBottomLayout.hideExView();

                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(inputEdit, 0);
                    RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.SCROLLBOTTOM);
                }
                return false;
            }
        });

        inputEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                checkSendState();
            }
        });
        inputEdit.addTextChangedListener(new TextWatcher() {
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
                if (isGroupAt && count == 1 && "@".equals(string.substring(start))) {
                    GroupAtActivity.startActivity((Activity) context, ((ChatActivity) context).getBaseChat().roomKey());
                }
            }
        });
    }

    public void isGroupAt(boolean at) {
        this.isGroupAt = at;
    }

    public void insertDraft(String draft) {
        int start = inputEdit.getSelectionStart();
        int end = inputEdit.getSelectionEnd();
        start = (start < 0 ? 0 : start);
        end = (start < 0 ? 0 : end);
        inputEdit.getText().replace(start, end, draft);
    }

    public String getDraft() {
        return inputEdit.getText().toString();
    }

    public void replaceEmoticons(Editable editable, int start, int count) {
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

    private void checkSendState() {
        String textMessage = inputEdit.getText().toString();
        if (TextUtils.isEmpty(textMessage)) {
            inputVoice.setVisibility(View.VISIBLE);
            inputTxt.setVisibility(View.GONE);
        } else {
            inputVoice.setVisibility(View.GONE);
            inputTxt.setVisibility(View.VISIBLE);
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.inputmore:
                    switchMoreView();
                    break;
                case R.id.inputface:
                    switchEmojView();
                    focusEdit();
                    break;
                case R.id.inputtxt:
                    String string = inputEdit.getText().toString();
                    if (RegularUtil.matches(string, RegularUtil.OUTER_BITWEBSITE)) {
                        String title = "";
                        String sub = "";
                        WebsiteExt1Bean webSite = new WebsiteExt1Bean();
                        if (RegularUtil.matches(string, RegularUtil.OUTER_BITWEBSITE_TRANSFER)) {//External transfer
                            title = context.getString(R.string.Wallet_Send_a_bitcoin_transfer_link);
                            sub = context.getString(R.string.Wallet_Click_to_recive_payment);
                        } else if (RegularUtil.matches(string, RegularUtil.OUTER_BITWEBSITE_PACKET)) {//inner lucky packet
                            title = context.getString(R.string.Wallet_Sent_via_link_luck_packet);
                            sub = context.getString(R.string.Chat_Send_a_Luck_Packet_Click_to_view);
                        } else if (RegularUtil.matches(string, RegularUtil.OUTER_BITWEBSITE_PAY)) {//external gather
                            title = context.getString(R.string.Wallet_Send_the_payment_connection);
                            sub = context.getString(R.string.Wallet_Click_to_transfer_bitcoin);
                        }

                        webSite.setExt1(title, sub);
                        MsgSend.sendOuterMsg(MsgType.OUTER_WEBSITE, string, webSite);
                    } else {
                        MsgSend.sendOuterMsg(MsgType.Text, inputEdit.getText().toString(), inputEdit.groupAts());
                    }
                    inputEdit.setText("");
                    break;
            }
        }
    };

    protected void switchMoreView() {
        exBottomLayout.switchMoreView();
        SystemUtil.hideKeyBoard(context, inputEdit);
    }

    public void switchEmojView() {
        exBottomLayout.switchEmojiView();
        SystemUtil.hideKeyBoard(context, inputEdit);
    }

    public void hideBottomPanel() {
        SystemUtil.hideKeyBoard(context, inputEdit);
        exBottomLayout.hideExView();
    }
}