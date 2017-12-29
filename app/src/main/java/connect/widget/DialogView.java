package connect.widget;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.chat.adapter.PickHoriScrollAdapter;
import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.view.PickHorScrollView;
import connect.ui.activity.R;
import connect.utils.system.SystemDataUtil;

/**
 * Recent pictures
 */

public class DialogView {

    public Dialog showPhotoPick(final Context context){
        final Dialog dialog = new Dialog(context, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_photopick, null);
        dialog.setContentView(view);

        final TextView library = (TextView)view.findViewById(R.id.photo_library);
        TextView cancel = (TextView)view.findViewById(R.id.cancel);
        final PickHorScrollView horScrollView= (PickHorScrollView) view.findViewById(R.id.scrollview);

        List<String> images = recentImages();

        horScrollView.setPickAdapter(new PickHoriScrollAdapter(context,images));
        horScrollView.setItemClickListener(new PickHorScrollView.OnItemClickListener() {
            @Override
            public void itemOnClick(List<String> paths) {
                if (paths.size() == 0) {
                    library.setText(context.getResources().getString(R.string.Chat_Photo_libary));
                } else {
                    library.setText(String.format(context.getString(R.string.Chat_Send_Mulphoto), paths.size()));
                }
            }
        });

        library.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (horScrollView.getClickLists().size() == 0) {
                    RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.OPEN_ALBUM);
                } else {
                    MsgSend.sendOuterMsg(MsgSend.MsgSendType.Photo,horScrollView.getClickLists());
                }
                dialog.cancel();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
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

    public List<String> recentImages() {
        List<String> recentImgs = new ArrayList<>();
        Context context = BaseApplication.getInstance().getBaseContext();
        String sdcardPath = Environment.getExternalStorageDirectory().toString();
        ContentResolver mContentResolver = context.getContentResolver();
        Cursor mCursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA},
                MediaStore.Images.Media.MIME_TYPE + "=? OR " + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media._ID + " DESC");

        while (mCursor.moveToNext()) {
            long id = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Images.Media._ID));
            String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            if (path.startsWith(sdcardPath + "/DCIM/100MEDIA") || path.startsWith(sdcardPath + "/DCIM/Camera/")
                    || path.startsWith(sdcardPath + "DCIM/100Andro")) {

                File file = new File(path);
                if (!file.exists() || !file.canRead() || file.length() < 5 * 1024) continue;
                recentImgs.add(path);
            }
        }
        mCursor.close();
        return recentImgs;
    }
}
