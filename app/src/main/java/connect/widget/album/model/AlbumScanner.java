package connect.widget.album.model;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import connect.ui.activity.R;
import connect.widget.album.inter.IAlbumScanner;
import connect.widget.album.presenter.AlbumPresenter;

/**
 * Created by Clock on 2016/3/21.
 */
public class AlbumScanner implements IAlbumScanner {

    private final static String TAG = "_ImageScannerModelImpl";
    /**
     * Loader's unique ID number
     */
    private final static int IMAGE_LOADER_ID = 1000;
    /**
     * Load data mapping
     */
    private final static String[] IMAGE_PROJECTION = new String[]{
            MediaStore.Images.Media.DATA,//Picture path
            MediaStore.Images.Media.DISPLAY_NAME,//Image file name, including the suffix
            MediaStore.Images.Media.TITLE//Image file name, does not contain suffix
    };

    private Context context;

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
     * @param albumFolderMap
     */
    public void searchLocalPhoto(HashMap<String, AlbumFolderInfo> albumFolderMap){
        Cursor cursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME}
                , null, MediaStore.Images.Media.DATE_ADDED + " DESC");
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String imagePath = cursor.getString(1);
            ExFile albumFolder = new ExFile(imagePath, 0);
            if(albumFolder.length() < 1024 * 5){
                continue;
            }

            //picture directory is already loaded into the list
            String albumPath = albumFolder.getParentFile().getName();
            AlbumFolderInfo folderInfo = albumFolderMap.get(albumPath);

            ImageInfo imageFile = new ImageInfo(albumFolder,0);
            if (folderInfo == null) {
                folderInfo = new AlbumFolderInfo(albumFolder, albumPath);
                folderInfo.setAlbumType(AlbumType.Photo);
                ArrayList<ImageInfo> albumImageFiles = new ArrayList<>();
                folderInfo.setImageInfoList(albumImageFiles);
                albumFolderMap.put(albumPath, folderInfo);
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

    public List<AlbumFolderInfo> sortAllAlbum(HashMap<String, AlbumFolderInfo> albumFolderMap){
        List<AlbumFolderInfo> albumFolderList = new ArrayList<>();
        for (AlbumFolderInfo info : albumFolderMap.values()) {
            albumFolderList.add(info);
        }
        sortByFileLastModified(albumFolderList);

        //Add folder ,all image and video
        AlbumFolderInfo allfolderInfo = null;
        for (AlbumFolderInfo info : albumFolderList) {
            if (allfolderInfo == null) {
                String allAlbumName = context.getString(R.string.Chat_All_Image_Video);
                allfolderInfo = new AlbumFolderInfo(info.getFrontCover(), allAlbumName);
                allfolderInfo.setAlbumType(AlbumType.All);
                ArrayList<ImageInfo> allImagesInfo = new ArrayList<>();
                allfolderInfo.setImageInfoList(allImagesInfo);
            }
            allfolderInfo.getImageInfoList().addAll(info.getImageInfoList());
        }
        if (allfolderInfo != null) {
            albumFolderList.add(0, allfolderInfo);
        }
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
