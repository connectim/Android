package connect.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import connect.ui.activity.R;

/**
 * Created by Administrator on 2018/1/5 0005.
 */

public class DepartmentAvatar extends RelativeLayout {

    private final Context context;
    private TextView nameTv;

    public DepartmentAvatar(Context context) {
        this(context, null);
    }

    public DepartmentAvatar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DepartmentAvatar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.view_department_avatar, this, true);
        initView(view);
    }

    private void initView(View view) {
        nameTv = (TextView)view.findViewById(R.id.name_tv);
    }

    public void setAvatarName(String name){
        if(name.length() > 2){
            this.setBackground(context.getResources().getDrawable(R.drawable.shape_8px_f27e32));
            nameTv.setText(name.substring(1,3));
        }else{
            this.setBackground(context.getResources().getDrawable(R.drawable.shape_8px_6b91ea));
            nameTv.setText(name);
        }
    }

}
