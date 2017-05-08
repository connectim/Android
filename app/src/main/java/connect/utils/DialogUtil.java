package connect.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.ui.activity.chat.model.more.MorePagerAdapter;
import connect.ui.activity.login.adapter.DialogBottomAdapter;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;
import connect.view.FrameAnimationDrawable;
import connect.view.payment.PayEditView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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
        if (!TextUtils.isEmpty(message)) {
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
     * ListView with Dialog (intermediate display)
     *
     * @param mContext
     * @param title
     * @param adapter
     * @param itemClickListener
     * @return
     */
    public static Dialog showAlertListView(Context mContext, Integer title, BaseAdapter adapter,
                                           final DialogListItemLongClickListener itemClickListener) {
        final Dialog dialog = new Dialog(mContext, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.view_listview, null);
        dialog.setContentView(view);
        TextView titleTextView = (TextView) view.findViewById(R.id.title_tv);
        ListView listView = (ListView) view.findViewById(R.id.list_view);
        LinearLayout list_lin = (LinearLayout) view.findViewById(R.id.list_lin);
        if (adapter != null) {
            list_lin.setVisibility(View.VISIBLE);
            listView.setVisibility(View.VISIBLE);
            listView.setAdapter(adapter);
        }

        ViewGroup.LayoutParams layoutParams = null;
        if (adapter.getCount() >= 5) {
            layoutParams = new LinearLayout.LayoutParams(SystemDataUtil.getScreenWidth(),
                    SystemUtil.dipToPx(450));
        } else {
            layoutParams = new LinearLayout.LayoutParams(SystemDataUtil.getScreenWidth(),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        list_lin.setLayoutParams(layoutParams);

        if (title != null) {
            titleTextView.setText(title);
        } else {
            titleTextView.setVisibility(View.GONE);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemClickListener.onClick(parent, view, position);
                dialog.dismiss();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                itemClickListener.onLongClick(parent, view, position);
                return true;
            }
        });

        Window mWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = mWindow.getAttributes();
        lp.width = SystemDataUtil.getScreenWidth();
        mWindow.setGravity(Gravity.BOTTOM);
        mWindow.setWindowAnimations(R.style.DialogAnim);
        mWindow.setAttributes(lp);
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    /**
     * burn message dialog
     */
    public static Dialog showBurnDialog(final Context mContext, final long sectime, final MorePagerAdapter.OnTimerListener listener) {
        final Dialog dialog = new Dialog(mContext, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_burn, null);
        dialog.setContentView(view);

        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.linearlayout);
        ListView listView = (ListView) view.findViewById(R.id.listview);

        ViewGroup.LayoutParams layoutParams = null;
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(layoutParams);

        final String[] strings = mContext.getResources().getStringArray(R.array.destruct_timer);
        final int[] destimes = mContext.getResources().getIntArray(R.array.destruct_timer_long);
        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return strings.length;
            }

            @Override
            public Object getItem(int position) {
                return strings[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_dialog_burn, null);
                }
                TextView time = (TextView) convertView.findViewById(R.id.txt);
                ImageView img = (ImageView) convertView.findViewById(R.id.img);

                time.setText(strings[position]);
                if (position == strings.length - 1) {
                    time.setTextColor(mContext.getResources().getColor(R.color.color_blue));
                } else {
                    time.setTextColor(mContext.getResources().getColor(R.color.color_black));
                }

                if (sectime == destimes[position]) {
                    img.setVisibility(View.VISIBLE);
                } else {
                    img.setVisibility(View.GONE);
                }
                return convertView;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.itemTimerClick(destimes[position]);
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
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
    public static Dialog showBottomListView(Context mContext, ArrayList<String> list, final DialogListItemClickListener itemClickListener) {
        final Dialog dialog = new Dialog(mContext, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_bottom_listview, null);
        dialog.setContentView(view);
        ListView listView = (ListView) view.findViewById(R.id.list_view);
        TextView cancel = (TextView) view.findViewById(R.id.tv_popup_cancel);
        DialogBottomAdapter dialogBottomAdapter = new DialogBottomAdapter(list);
        listView.setAdapter(dialogBottomAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemClickListener.confirm(parent, view, position);
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

        void confirm(AdapterView<?> parent, View view, int position);

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
