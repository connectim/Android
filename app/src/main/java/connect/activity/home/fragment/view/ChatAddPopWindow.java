package connect.activity.home.fragment.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import connect.activity.home.bean.HomeAction;
import connect.activity.set.bean.SystemSetBean;
import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;

/**
 * Created by PuJin on 2018/1/18.
 */

public class ChatAddPopWindow extends PopupWindow {

    private View conentView;

    public ChatAddPopWindow(final Activity activity) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        conentView = inflater.inflate(R.layout.popupwindow_chatadd, null);
        int w = activity.getWindowManager().getDefaultDisplay().getWidth();
        int h = activity.getWindowManager().getDefaultDisplay().getHeight();
        // 设置SelectPicPopupWindow的View
        this.setContentView(conentView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(w / 2 - 60);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        // 刷新状态
        this.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);
        // mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimationPreview);

        SystemSetBean systemSetBean = ParamManager.getInstance().getSystemSet();
        String notifyTxt = systemSetBean.isRing() && systemSetBean.isVibrate() ?
                activity.getString(R.string.Link_Close_to_remind):
                activity.getString(R.string.Link_Open_to_remind);
        ((TextView) conentView.findViewById(R.id.popup_turn_notify)).setText(notifyTxt);

        conentView.findViewById(R.id.new_chat_linear).setOnClickListener(onClickListener);
        conentView.findViewById(R.id.scan_linear).setOnClickListener(onClickListener);
        conentView.findViewById(R.id.voice_linear).setOnClickListener(onClickListener);
        conentView.findViewById(R.id.help_linear).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.new_chat_linear:
                    HomeAction.getInstance().sendEvent(HomeAction.HomeType.GROUP_NEWCHAT, 1);
                    dismiss();
                    break;
                case R.id.scan_linear:
                    HomeAction.getInstance().sendEvent(HomeAction.HomeType.GROUP_NEWCHAT, 2);
                    dismiss();
                    break;
                case R.id.voice_linear:
                    HomeAction.getInstance().sendEvent(HomeAction.HomeType.GROUP_NEWCHAT, 3);
                    dismiss();
                    break;
                case R.id.help_linear:
                    HomeAction.getInstance().sendEvent(HomeAction.HomeType.GROUP_NEWCHAT, 4);
                    dismiss();
                    break;
                default:
                    break;
            }
        }
    };

}
