package connect.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import connect.activity.base.BaseActivity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;

public class TransferMutiDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_muti_detail);
    }

    public static void startActivity(Activity activity, String hashid, String msgid) {
        Bundle bundle = new Bundle();
        bundle.putString("HASHID", hashid);
        bundle.putString("MESSAGEID", msgid);
        ActivityUtil.next(activity, TransferMutiDetailActivity.class, bundle);
    }

    @Override
    public void initView() {

    }
}
