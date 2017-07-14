package connect.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import connect.activity.login.adapter.DialogBottomAdapter;
import connect.ui.activity.R;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;
import connect.widget.FrameAnimationDrawable;
import connect.widget.payment.PayEditView;

/**
 * Dialog Tooltip tool
 * Created by Administrator on 2016/8/18.
 */
public class DialogUtil {

    /**
     * Text prompt
     *
     * @param mContext
     * @param title
     * @param message
     * @param cancelButton
     * @param okButton
     * @param iscancel
     * @param onItemClickListener
     * @return
     */
    public static Dialog showAlertTextView(Context mContext, String title,
                                           String message, String cancelButton, String okButton, boolean iscancel,
                                           final OnItemClickListener onItemClickListener) {
        return showAlertTextView(mContext,title,message,cancelButton,okButton,iscancel,onItemClickListener,true);
    }

    public static Dialog showAlertTextView(Context mContext, String title,
                                           String message, String cancelButton, String okButton, boolean iscancel,
                                           final OnItemClickListener onItemClickListener,boolean Outside) {
        final Dialog dialog = new Dialog(mContext, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_hint, null);
        dialog.setContentView(view);
        TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        TextView messageTextView = (TextView) view.findViewById(R.id.messageTextView);
        TextView cancelBtn = (TextView) view.findViewById(R.id.cancelBtn);
        TextView oklBtn = (TextView) view.findViewById(R.id.okBtn);

        if (TextUtils.isEmpty(title)) {
            titleTextView.setVisibility(View.GONE);
        } else {
            titleTextView.setText(title);
        }

        messageTextView.setText(message);
        if (iscancel) {
            view.findViewById(R.id.centre_line).setVisibility(View.GONE);
            cancelBtn.setVisibility(View.GONE);
        } else {
            if(!TextUtils.isEmpty(cancelButton))
                cancelBtn.setText(cancelButton);
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.cancel();
                    dialog.cancel();
                }
            });
        }

        if(!TextUtils.isEmpty(okButton))
            oklBtn.setText(okButton);
        oklBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.confirm("");
                dialog.cancel();
            }
        });

        Window mWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = mWindow.getAttributes();
        lp.width = SystemUtil.dipToPx(250);
        mWindow.setAttributes(lp);
        dialog.setCanceledOnTouchOutside(Outside);
        dialog.show();
        return dialog;
    }

    /**
     * EditView diaolog
     *
     * @param mContext
     * @param title
     * @param leftStr
     * @param rightStr
     * @param hinit
     * @param onItemClickListener
     * @return
     */
    public static Dialog showEditView(Context mContext, String title, String leftStr, String rightStr,
                                      String message, String hinit,String text,boolean isGone ,int maxLength,final OnItemClickListener onItemClickListener) {
        final Dialog dialog = new Dialog(mContext, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_edit, null);
        TextView titleTv = (TextView) view.findViewById(R.id.title_tv);
        final EditText edit = (EditText) view.findViewById(R.id.edit);
        TextView leftTv = (TextView) view.findViewById(R.id.left_tv);
        TextView rightTv = (TextView) view.findViewById(R.id.right_tv);
        TextView messageTv = (TextView) view.findViewById(R.id.message_tv);

        if(isGone)
            edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        titleTv.setText(title);
        if (TextUtils.isEmpty(message)) {
            messageTv.setVisibility(View.GONE);
        }else{
            messageTv.setText(message);
            messageTv.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(hinit)) {
            edit.setHint(hinit);
        }
        if (!TextUtils.isEmpty(leftStr)) {
            leftTv.setText(leftStr);
        }
        if (!TextUtils.isEmpty(rightStr)) {
            rightTv.setText(rightStr);
        }
        if(!TextUtils.isEmpty(text)){
            edit.setText(text);
        }
        if(maxLength > 0){
            edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        }

        leftTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        rightTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.confirm(edit.getText().toString());
                dialog.cancel();
            }
        });

        view.setMinimumWidth(SystemUtil.dipToPx(250));
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(view);
        dialog.show();

        SystemUtil.showKeyBoard(edit.getContext(),edit);
        return dialog;
    }

    /**
     * input passwrod Dialog
     *
     * @param mContext
     * @param title
     * @param message
     * @param onItemClickListener
     * @return
     */
    public static Dialog showPayEditView(Context mContext, Integer title, Integer message, final OnItemClickListener onItemClickListener) {
        final Dialog dialog = new Dialog(mContext, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_paypass, null);
        TextView titleTv = (TextView) view.findViewById(R.id.title_tv);
        TextView messageTv = (TextView) view.findViewById(R.id.message_tv);
        final PayEditView payEditView = (PayEditView) view.findViewById(R.id.payEditView);

        titleTv.setText(title);
        if (message != null) {
            messageTv.setText(message);
            messageTv.setVisibility(View.VISIBLE);
        }

        payEditView.setInputCompleteListener(new PayEditView.InputCompleteListener() {
            @Override
            public void inputComplete(String pass) {
                onItemClickListener.confirm(pass);
                dialog.dismiss();
            }
        });

        view.setMinimumWidth(SystemUtil.dipToPx(250));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);
        dialog.show();

        payEditView.editText.setFocusableInTouchMode(true);
        payEditView.editText.requestFocus();
        SystemUtil.showKeyBoard(mContext,payEditView.editText);

        return dialog;
    }

    /**
     * Payment dialog
     *
     * @return
     */
    public static Dialog showConnectPay(Context context) {
        final Dialog dialog = new Dialog(context, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_processpay, null);

        final View dotleft = view.findViewById(R.id.dot_left);
        final View dotcentre = view.findViewById(R.id.dot_centre);
        final View dotrigth = view.findViewById(R.id.dot_right);

        final Handler timetHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                dotleft.setSelected(false);
                dotcentre.setSelected(false);
                dotrigth.setSelected(false);
                switch (msg.what) {
                    case 0:
                        dotleft.setSelected(true);
                        break;
                    case 1:
                        dotcentre.setSelected(true);
                        break;
                    case 2:
                        dotrigth.setSelected(true);
                        break;
                }
            }
        };

        final Timer timer = new Timer();
        final TimerTask timerTask = new TimerTask() {
            int i = 0;
            @Override
            public void run() {
                timetHandler.sendEmptyMessage(i % 3);
                i++;
                if(i > 20){
                    timer.cancel();
                }
            }
        };
        timer.schedule(timerTask, 200, 500);

        view.setMinimumWidth(SystemUtil.dipToPx(130));
        dialog.setContentView(view);
        dialog.show();
        return dialog;
    }

    /**
     * popup dialong
     *
     * @param mContext
     * @param list
     * @param itemClickListener
     * @return
     */
    public static Dialog showBottomView(Context mContext, ArrayList<String> list, final DialogListItemClickListener itemClickListener) {
        final Dialog dialog = new Dialog(mContext, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_bottomview, null);
        dialog.setContentView(view);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        TextView cancel = (TextView) view.findViewById(R.id.tv_popup_cancel);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(linearLayoutManager);
        DialogBottomAdapter dialogBottomAdapter = new DialogBottomAdapter(list);
        recyclerView.setAdapter(dialogBottomAdapter);
        dialogBottomAdapter.setItemClickListener(new DialogBottomAdapter.OnItemClickListener() {
            @Override
            public void itemClick(int position, String string) {
                itemClickListener.confirm(position);
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Window mWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = mWindow.getAttributes();
        lp.width = SystemDataUtil.getScreenWidth();
        mWindow.setGravity(Gravity.BOTTOM);
        mWindow.setWindowAnimations(R.style.DialogAnim);
        mWindow.setAttributes(lp);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        return dialog;
    }

    public interface OnGifListener{
        void click();
    }

    /**
     * lucky packet pop animation
     * @param context
     * @param state R.drawable.anim_redpacket_success, R.drawable.anim_redpacket_fail
     * @return
     */
    public static Dialog showRedPacketState(final Context context, final int state, final OnGifListener listener) {
        final Dialog dialog = new Dialog(context, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(state == 0 ? R.layout.dialog_redpacket_fail : R.layout.dialog_redpacket_success, null);
        dialog.setContentView(view);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(SystemDataUtil.getScreenWidth(), SystemDataUtil.getScreenHeight());
        view.setLayoutParams(layoutParams);

        ImageView img = (ImageView) view.findViewById(R.id.img);
        String title = 0 == state ? context.getString(R.string.Wallet_Unfortunately) : context.getString(R.string.Wallet_Congratulations);
        String subtitle = 0 == state ? context.getString(R.string.Wallet_Good_luck_next_time) : context.getString(R.string.Wallet_You_got_a_Lucky_Packet);
        (((TextView) (view.findViewById(R.id.txt1)))).setText(title);
        (((TextView) (view.findViewById(R.id.txt2)))).setText(subtitle);

        view.findViewById(R.id.img1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.click();
                dialog.dismiss();
            }
        });
        FrameAnimationDrawable.animateDrawableLoad(state==0?R.drawable.anim_redpacket_fail:R.drawable.anim_redpacket_success, img, new FrameAnimationDrawable.OnDrawablesListener() {

            @Override
            public void onDrawsStart() {
            }

            @Override
            public void onDrawsStop() {
                view.findViewById(R.id.linearlayout).setVisibility(View.VISIBLE);
                view.findViewById(R.id.btn1).setVisibility(View.VISIBLE);
            }
        });

        dialog.show();
        return dialog;
    }

    public interface DialogListItemClickListener {

        void confirm(int position);

    }

    public interface DialogListItemLongClickListener {

        void onClick(AdapterView<?> parent, View view, int position);

        void onLongClick(AdapterView<?> parent, View view, int position);

    }

    public interface OnItemClickListener {
        void confirm(String value);

        void cancel();
    }
}
