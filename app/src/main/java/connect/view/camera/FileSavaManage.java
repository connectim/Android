package connect.view.camera;

import android.os.AsyncTask;

import connect.utils.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2016/12/13.
 */
public class FileSavaManage {

    public File getPhotoFile(byte[] data){
        File imageFile = FileUtil.newTempFile(FileUtil.FileType.IMG);
        if (null != imageFile) {
            try {
                FileOutputStream fos = new FileOutputStream(imageFile);
                fos.write(data);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imageFile;
    }

    public void deleFile(final File file){
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                FileUtil.deleteFile(file.getPath());
                return null;
            }
        }.execute();
    }

}
