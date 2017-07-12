package connect.widget.payment;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import connect.ui.activity.R;

/**
 * Pay the password input box
 * Created by Administrator on 2016/12/7.
 */
public class PayEditView extends RelativeLayout {

    private ImageView[] imageViews;
    public EditText editText;
    private StringBuffer stringBuffer = new StringBuffer();//Store password characters
    private InputCompleteListener inputCompleteListener;

    public PayEditView(Context context) {
        this(context, null);
    }

    public PayEditView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PayEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        imageViews = new ImageView[4];
        View view = View.inflate(context, R.layout.item_pay_edit, this);
        editText = (EditText) findViewById(R.id.item_edittext);
        imageViews[0] = (ImageView) findViewById(R.id.item_password_iv1);
        imageViews[1] = (ImageView) findViewById(R.id.item_password_iv2);
        imageViews[2] = (ImageView) findViewById(R.id.item_password_iv3);
        imageViews[3] = (ImageView) findViewById(R.id.item_password_iv4);
        editText.setCursorVisible(false);

        setListener();
    }

    private void setListener() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Enter the characters to delete to delete the corresponding position
                if (editable.toString().equals("-")) {
                    editText.setText("");
                    onKeyDelete();
                    return;
                }

                //Focus if the character is not ""
                if (!editable.toString().equals("")) {
                    if (stringBuffer.length() > 3) {
                        editText.setText("");
                        return;
                    }

                    stringBuffer.append(editable);
                    editText.setText("");//After adding the EditText will be empty to create the wrong word input
                    for (int i = 0; i < stringBuffer.length(); i++) {
                        imageViews[i].setImageResource(R.drawable.shape_circle_6d6e75);
                    }

                    if (stringBuffer.length() == 4 && inputCompleteListener != null) {
                        inputCompleteListener.inputComplete(stringBuffer.toString());
                        //Text length bit 4 calls to complete the input listener
                        stringBuffer.delete(0, stringBuffer.length());
                        for (int i = 0; i < 4; i++) {
                            imageViews[i].setImageResource(R.drawable.shape_ring_6d6e75);
                        }
                    }
                }
            }
        });

        // Monitoring software disc delete button
        editText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    return onKeyDelete();
                    /*if (onKeyDelete()) return true;
                        return true;*/
                }
                return false;
            }
        });
    }

    /**
     * When press the delete button, remove the string in the StringBuffer
     * @return
     */
    public boolean onKeyDelete() {
        int count = stringBuffer.length();
        if (count == 0) {
            return true;
        } else {
            //Delete character
            stringBuffer.delete((count - 1), count);
            imageViews[stringBuffer.length()].setImageResource(R.drawable.shape_ring_6d6e75);
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    public void setInputCompleteListener(InputCompleteListener inputCompleteListener) {
        this.inputCompleteListener = inputCompleteListener;
    }

    public interface InputCompleteListener {
        void inputComplete(String pass);
    }

    public EditText getEditText() {
        return editText;
    }

    /**
     * EditView Get focus
     */
    public void requestOpenPan() {
        editText.requestFocus();
    }

    public void setEditClosePan() {
        editText.setInputType(InputType.TYPE_NULL);
    }

    public void setEditText(String value) {
        editText.setText(value);
    }

    public void setContent(String content) {
        editText.setText(content);
    }

}
