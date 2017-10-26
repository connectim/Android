package connect.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import connect.ui.activity.R;
import connect.activity.chat.bean.RoomSession;
import connect.activity.base.BaseApplication;

public class FileUtil {

    /** app */
    private static String APP_ROOT_NAME = "connect";
    /** Root path for local file */
    public static String DIR_ROOT = getExternalStorePath();
    /** Temporary folder */
    public static final String DIR_TEMP = "temp" + File.separator;

    /**
     * Get the private file path
     * @return
     */
    public static String getExternalStorePath() {
        String dir = null;
        if (ConfigUtil.getInstance().appMode()) {
            dir = BaseApplication.getInstance().getDir(APP_ROOT_NAME, Context.MODE_PRIVATE).getPath();
        } else {
            dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                    + BaseApplication.getInstance().getString(R.string.app_name);
        }
        return dir;
    }

    /**
     * ==========================================================================
     *                              Create file folder
     * ==========================================================================
     */
    /**
     * Random file name
     * @return
     */
    public static String randomFileName() {
        return "" + TimeUtil.getCurrentTimeInLong() + (int) (Math.random() * 1000);
    }

    /**
     * Create contact's file
     * @param type
     */
    public static File newContactFile(FileType type) {
        String index = randomFileName();
        index = index + type.getFileType();

        String contactKey = RoomSession.getInstance().getRoomKey();
        index = TextUtils.isEmpty(contactKey) ? index : contactKey + "/" + index;
        return createNewFile(index);
    }

    /**
     * Create temporary file when exiting the application
     * @param type
     * @return
     */
    public static File newTempFile(FileType type) {
        String index = randomFileName();
        index = index + type.getFileType();
        return createAbsNewFile(DIR_ROOT + File.separator +index);
    }

    /**
     * create new file
     * @param path
     * @return
     */
    public static File createNewFile(String path) {
        path = DIR_ROOT + File.separator + path;
        return createAbsNewFile(path);
    }

    public static File createAbsNewFile(String path) {
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                return null;
            }
        }
        try {
            if (!file.exists() && !file.createNewFile()) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
        return file;
    }

    /**
     * contact file
     * @param pubKey
     * @param filename
     * @param fileType
     * @return
     */
    public static String newContactFileName(String pubKey, String filename, FileType fileType) {
        return DIR_ROOT + File.separator + pubKey + File.separator + filename + fileType.getFileType();
    }

    /**
     * ==========================================================================
     *                              file operation
     * ==========================================================================
     */
    public static boolean isLocalFile(String path) {
        return !RegularUtil.matches(path, RegularUtil.VERIFICATION_HTTP);
    }

    public static String realFileName(String filename) {
        int dot = filename.lastIndexOf(File.separator) + 1;
        return filename.substring(dot);
    }

    public static boolean isExistFilePath(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static boolean isExistExternalStore() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * To determine whether the folder (with a suffix)
     * @param filename
     * @return
     */
    public static boolean hasExtentsion(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot > -1) && (dot < (filename.length() - 1));
    }

    public static String subExtentsion(String filename) {
        int dot = filename.lastIndexOf('.');
        return filename.substring(0, dot);
    }

    /**
     * Get file
     * @param data
     * @param fileType
     * @return
     */
    public static File byteArrayToFile(byte[] data,FileUtil.FileType fileType){
        File imageFile = FileUtil.newTempFile(fileType);
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

    /**
     * Get file byte array
     * @param filePath
     * @return
     */
    public static byte[] filePathToByteArray(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * file size
     * @param path
     * @return
     */
    public static long fileSize(FileSizeType fileSizeType, String path) {
        if (path == null)
            return 0;
        File file = new File(path);
        if (!file.exists()) {
            return 0;
        }

        long length=file.length();
        switch (fileSizeType){
            case KB:
                length=length/1024;
                break;
            case M:
                length=length/(1024*1024);
                break;
        }
        return length;
    }

    public static String fileSize(String path) {
        return fileSize(FileSizeType.KB, path)+" KB";
    }

    public static int fileSizeOf(String path) {
        if (path == null)
            return 0;
        File file = new File(path);
        if (!file.exists()) {
            return 0;
        }
        return (int) (file.length() / 1024);
    }

    public static String fileSize(int length) {
        String size = "";
        FileSizeType sizeType = length < 1024 ? FileSizeType.KB : FileSizeType.M;
        switch (sizeType) {
            case KB:
                size = length + " KB";
                break;
            case M:
                size = length / 1024 + " M";
                break;
        }
        return size;
    }

    public enum FileSizeType{
        KB,
        M,
    }

    /**
     * ==========================================================================
     *                              delete file
     * ==========================================================================
     */

    /**
     * Delete contact file
     *
     * @param pubkey
     */
    public static boolean deleteContactFile(String pubkey) {
        String path = DIR_ROOT + File.separator + pubkey;
        return deleteDirectory(path);
    }

    /**
     * delete file
     */
    public static boolean deleteFile(String filePath) {
        boolean flag = false;
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

    /**
     * delete direction
     *
     * @param filePath
     * @return
     */
    public static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag)
            return false;
        return dirFile.delete();
    }
    /**
     * ==========================================================================
     *                              Save data to file
     * ==========================================================================
     */
    /**
     * Store image to local
     * @return local Path
     */
    public static void byteArrToFilePath(byte[] bfile, String filePath) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            file = createAbsNewFile(filePath);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * file type
     */
    public enum FileType {
        IMG(".png"), VOICE(".aac"), VIDEO(".mp4");

        String fileType;

        FileType(String type) {
            this.fileType = type;
        }

        public String getFileType() {
            return fileType;
        }
    }

}
