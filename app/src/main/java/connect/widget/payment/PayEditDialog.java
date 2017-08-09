package connect.widget.payment;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.utils.DialogUtil;
import connect.utils.system.SystemUtil;

/**
 * Created by Administrator on 2017/7/10 0010.
 */

public class PayEditDialog {

    public static Dialog showPayEditView(Context mContext, Integer title, Integer message, final DialogUtil.OnItemClickListener onItemClickListener) {
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

}
