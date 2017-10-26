package connect.activity.wallet.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import connect.ui.activity.R;

import java.util.ArrayList;

public class VirtualKeyboardView extends LinearLayout {

    Context context;
    private GridView gridView;
    private ArrayList<String> valueList;
    private AddNumberListener addNumberListener = null;

    public VirtualKeyboardView(Context context) {
        this(context, null);
    }

    public VirtualKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        View view = View.inflate(context, R.layout.layout_virtual_keyboard, null);
        gridView = (GridView) view.findViewById(R.id.gv_keybord);
        gridView.setOnItemClickListener(onItemClickListener);
        setView();
        addView(view);
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(position == 11){
                //close
                addNumberListener.changeText("-");
            }else{
                String value = (String)parent.getAdapter().getItem(position);
                if(!TextUtils.isEmpty(value)){
                    addNumberListener.changeText(value);
                }
            }
        }
    };

    private void setView() {
        /* Initializes the button should be displayed on the Numbers */
        valueList = new ArrayList<>();
        for (int i = 1; i < 13; i++) {
            String value = "";
            if (i < 10) {
                value = String.valueOf(i);
            } else if (i == 10) {
                value = "";
            } else if (i == 11) {
                value = String.valueOf(0);
            } else if (i == 12) {
                value = "";
            }
            valueList.add(value);
        }
        KeyBoardAdapter keyBoardAdapter = new KeyBoardAdapter(context, valueList);
        gridView.setAdapter(keyBoardAdapter);
    }

    public void setAddNumberListener(AddNumberListener addNumberListener){
        this.addNumberListener = addNumberListener;
    }

    public interface AddNumberListener {

        void changeText(String value);

    }

}
