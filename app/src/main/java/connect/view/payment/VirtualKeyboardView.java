package connect.view.payment;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import connect.ui.activity.R;

import java.util.ArrayList;
/**
 * Created by Administrator on 2016/12/23.
 */
public class VirtualKeyboardView extends LinearLayout {

    Context context;
    private GridView gridView;
    private RelativeLayout layoutBack;
    private ArrayList<String> valueList;
    private AddNumberListence addNumberListence = null;

    public VirtualKeyboardView(Context context) {
        this(context, null);
    }

    public VirtualKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        View view = View.inflate(context, R.layout.layout_virtual_keyboard, null);
        valueList = new ArrayList<>();
        gridView = (GridView) view.findViewById(R.id.gv_keybord);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 11){//close
                    addNumberListence.changeText("-");
                }else{
                    String value = (String)parent.getAdapter().getItem(position);
                    if(!TextUtils.isEmpty(value)){
                        addNumberListence.changeText(value);
                    }
                }
            }
        });
        setView();
        addView(view);
    }

    public RelativeLayout getLayoutBack() {
        return layoutBack;
    }

    public ArrayList<String> getValueList() {
        return valueList;
    }

    public GridView getGridView() {
        return gridView;
    }

    private void setView() {
        /* Initializes the button should be displayed on the Numbers */
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

    public void setAddNumberListence(AddNumberListence addNumberListence){
        this.addNumberListence = addNumberListence;
    }

    public interface AddNumberListence{

        void changeText(String value);

    }

}
