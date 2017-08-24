package connect.widget.album.model;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connect.activity.chat.bean.MsgSend;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.utils.ToastEUtil;
import connect.utils.log.LogManager;
import connect.utils.log.Logger;
import connect.widget.album.inter.IAlbumScanner;
import connect.widget.album.presenter.AlbumPresenter;

/**
 * Created by Clock on 2016/3/21.
 */
public class AlbumScanner implements IAlbumScanner {

    private final static String Tag = "_AlbumScanner";

    private Context context;
    private Map<String, ImageInfo> imageInfoMap = new HashMap<>();

    @Override
    public void startScanAlbum(final Context context, final AlbumType albumType, final AlbumPresenter.OnScanListener onScanListener) {
        this.context = context;

        new AsyncTask<Void, Void, List<AlbumFolderInfo>>() {
            @Override
            protected List<AlbumFolderInfo> doInBackground(Void... params) {
                HashMap<String, AlbumFolderInfo> albumFolderMap = new HashMap<>();
                switch (albumType) {
                    case Photo:
                        searchLocalPhoto(albumFolderMap);
                        break;
                    case Video:
                        break;
                    case All:
                        searchLocalPhoto(albumFolderMap);
                        searchLocalVideo(albumFolderMap);
                        break;
                    default:
                        break;
                }

                List<AlbumFolderInfo> albumFolderList = sortAllAlbum(albumFolderMap);
                if (!imageInfoMap.isEmpty()) {
                    ImageInfo coverInfo = null;
                    ArrayList<ImageInfo> allImagesInfo = new ArrayList<>();
                    for (ImageInfo imageInfo : imageInfoMap.values()) {
                        allImagesInfo.add(imageInfo);
                        if (coverInfo == null) {
                            coverInfo = imageInfo;
                        }
                    }

                    String allAlbumName = context.getString(R.string.Chat_All_Image_Video);
                    AlbumFolderInfo allfolderInfo = new AlbumFolderInfo(coverInfo.getImageFile(), allAlbumName);
                    allfolderInfo.setAlbumType(AlbumType.All);
                    allfolderInfo.setImageInfoList(allImagesInfo);
                    albumFolderList.add(0, allfolderInfo);
                }
                return albumFolderList;
            }

            @Override
            protected void onPostExecute(List<AlbumFolderInfo> albumFolderInfos) {
                super.onPostExecute(albumFolderInfos);
                if (onScanListener != null && albumFolderInfos != null) {
                    onScanListener.onScanFinish(albumFolderInfos);
                }
            }
        }.execute();
    }

    /**
     * Query local image
     *
     * @param albumFolderMap
     */
    public void searchLocalPhoto(HashMap<String, AlbumFolderInfo> albumFolderMap) {
        Cursor cursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME}
                , null, MediaStore.Images.Media.DATE_ADDED + " DESC");

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String imagePath = cursor.getString(1);

            ExFile albumFolder = new ExFile(imagePath, 0);
            if (albumFolder.length() < 1024 * 5) {
                continue;
            }

            //picture directory is already loaded into the list
            String albumName = albumFolder.getParentFile().getName();
            ImageInfo imageFile = new ImageInfo(albumFolder, 0);

            if (imageInfoMap.containsKey(imagePath)) {
                MsgSend.sendOuterMsg(MsgType.Text, imagePath);
            }
            imageInfoMap.put(imagePath, imageFile);

            AlbumFolderInfo folderInfo = albumFolderMap.get(albumName);
            if (folderInfo == null) {
                folderInfo = new AlbumFolderInfo(albumFolder, albumName);
                folderInfo.setAlbumType(AlbumType.Photo);
                ArrayList<ImageInfo> albumImageFiles = new ArrayList<>();
                folderInfo.setImageInfoList(albumImageFiles);
                albumFolderMap.put(albumName, folderInfo);
            }
            folderInfo.getImageInfoList().add(imageFile);
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * query local video
     */
    public void searchLocalVideo(HashMap<String, AlbumFolderInfo> albumFolderMap) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.SIZE, MediaStore.Video.Media.TITLE, MediaStore.Video.Media.MIME_TYPE},
                null, null, MediaStore.Video.Media.DATE_ADDED + " DESC");

        //Add folder video
        AlbumFolderInfo videosInfo = null;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String imagePath = cursor.getString(1);

            ExFile albumFolder = new ExFile(imagePath, 0);
            albumFolder.setVideoLength(cursor.getLong(2));
            long size = cursor.getLong(3);
            if (size > 1024 * 1024 * 10 || size < 1024 * 100) {
                continue;
            }

            //Picture directory is already loaded into the list
            String albumPath = albumFolder.getParentFile().getName();
            AlbumFolderInfo folderInfo = albumFolderMap.get(albumPath);

            ImageInfo imageFile = new ImageInfo(albumFolder, 1);
            if (imageInfoMap.containsKey(imagePath)) {
                MsgSend.sendOuterMsg(MsgType.Text, imagePath);
            }
            imageInfoMap.put(imagePath, imageFile);

            if (folderInfo == null) {
                folderInfo = new AlbumFolderInfo(albumFolder, albumPath);
                folderInfo.setAlbumType(AlbumType.Video);
                ArrayList<ImageInfo> albumImageFiles = new ArrayList<>();
                folderInfo.setImageInfoList(albumImageFiles);
                albumFolderMap.put(albumPath, folderInfo);
            }

            if (videosInfo == null) {
                String videoAlbumName = context.getString(R.string.Chat_All_Video);
                videosInfo = new AlbumFolderInfo(albumFolder, videoAlbumName);
                videosInfo.setAlbumType(AlbumType.Video);
                ArrayList<ImageInfo> videoImagesInfo = new ArrayList<>();
                videosInfo.setImageInfoList(videoImagesInfo);
                albumFolderMap.put(videoAlbumName, videosInfo);
            }
            folderInfo.getImageInfoList().add(imageFile);
            videosInfo.getImageInfoList().add(imageFile);
        }
        cursor.close();
    }

    public List<AlbumFolderInfo> sortAllAlbum(HashMap<String, AlbumFolderInfo> albumFolderMap) {
        List<AlbumFolderInfo> albumFolderList = new ArrayList<>();
        for (AlbumFolderInfo info : albumFolderMap.values()) {
            albumFolderList.add(info);
        }
        sortByFileLastModified(albumFolderList);
        return albumFolderList;
    }

    /**
     * In accordance with the file to modify the time to sort, the more recent changes in the row before the more
     */
    private void sortByFileLastModified(List<AlbumFolderInfo> files) {
        Collections.sort(files, new Comparator<AlbumFolderInfo>() {
            @Override
            public int compare(AlbumFolderInfo lhs, AlbumFolderInfo rhs) {
                if (lhs.getFrontCover().lastModified() > rhs.getFrontCover().lastModified()) {
                    return -1;
                } else if (lhs.getFrontCover().lastModified() < rhs.getFrontCover().lastModified()) {
                    return 1;
                }
                return 0;
            }
        });
    }
}
