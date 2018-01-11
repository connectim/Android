package connect.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.utils.StringUtil;

/**
 * Created by Administrator on 2018/1/5 0005.
 */

public class DepartmentAvatar extends RelativeLayout {

    private final Context context;
    private TextView nameTv;
    private TextView inactiveTv;

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
        inactiveTv = (TextView)view.findViewById(R.id.inactive_tv);
    }

    public void setAvatarName(String name, boolean isShow, int gender){
        if(StringUtil.checkZh(name)){
            if(name.length() > 2){
                nameTv.setText(name.substring(0,2).toUpperCase());
            }else{
                nameTv.setText(name.toUpperCase());
            }
        }else{
            if(name.length() > 2){
                nameTv.setText(name.substring(1,3));
            }else{
                nameTv.setText(name);
            }
        }

        if(gender == 1){
            this.setBackground(context.getResources().getDrawable(R.drawable.shape_8px_6b91ea));
        }else{
            this.setBackground(context.getResources().getDrawable(R.drawable.shape_8px_f27e32));
        }

        if(isShow){
            inactiveTv.setVisibility(VISIBLE);
            this.setBackground(context.getResources().getDrawable(R.drawable.shape_8px_366b91ea));
        }else{
            inactiveTv.setVisibility(GONE);
        }
    }

}
