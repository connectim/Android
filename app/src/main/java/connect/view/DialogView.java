package connect.view;

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

import java.util.ArrayList;
import java.util.List;

import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgSend;
import connect.ui.activity.chat.bean.RecExtBean;
import connect.ui.activity.chat.view.PickHorScrollView;
import connect.ui.activity.chat.adapter.PickHoriScrollAdapter;
import connect.ui.base.BaseApplication;
import connect.utils.system.SystemDataUtil;

/**
 * Recent pictures
 * Created by john on 2016/11/21.
 */

public class DialogView {

    /**
     * List entry click callback
     */
    public interface OnItemClick{

        void onClick(int position,Object countryCode);

    }

    public Dialog showPhotoPick(final Context context){
        final Dialog dialog = new Dialog(context, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_photopick, null);
        dialog.setContentView(view);

        final TextView library = (TextView)view.findViewById(R.id.photo_library);
        TextView cancel = (TextView)view.findViewById(R.id.cancel);
        final PickHorScrollView horScrollView= (PickHorScrollView) view.findViewById(R.id.scrollview);

        List<String> imgs = recentImgs();

        horScrollView.setPickAdapter(new PickHoriScrollAdapter(context,imgs));
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
                    RecExtBean.sendRecExtMsg(RecExtBean.ExtType.OPEN_ALBUM);
                } else {
                    MsgSend.sendOuterMsg(MsgType.Photo,horScrollView.getClickLists());
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

    public List<String> recentImgs() {
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
            // Do not need to filter out the picture, only to get a picture of the photo album
            String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            if (path.startsWith(sdcardPath + "/DCIM/100MEDIA") || path.startsWith(sdcardPath + "/DCIM/Camera/")
                    || path.startsWith(sdcardPath + "DCIM/100Andro")) {
                //recentImgs.add("file://" + path);
                recentImgs.add(path);
            }
        }
        mCursor.close();
        return recentImgs;
    }
}
