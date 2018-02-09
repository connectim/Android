package connect.widget.album.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connect.ui.activity.R;
import connect.widget.album.inter.IAlbumScanner;
import connect.widget.album.presenter.AlbumPresenter;

/**
 * Created by Clock on 2016/3/21.
 */
public class AlbumScanner implements IAlbumScanner {

    private final static String Tag = "_AlbumScanner";

    private Context context;

    /**
     * Image attribute.
     */
    private static final String[] IMAGES = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.LATITUDE,
            MediaStore.Images.Media.LONGITUDE,
            MediaStore.Images.Media.SIZE
    };

    /**
     * Video attribute.
     */
    private static final String[] VIDEOS = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.LATITUDE,
            MediaStore.Video.Media.LONGITUDE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.RESOLUTION
    };


    @Override
    public void startScanAlbum(final Context context, final AlbumFolderType albumFolderType, final AlbumPresenter.OnScanListener onScanListener) {
        this.context = context;

        new AsyncTask<Void, Void, List<AlbumFolder>>() {
            @Override
            protected List<AlbumFolder> doInBackground(Void... params) {
                ArrayList<AlbumFolder> albumFolders;
                switch (albumFolderType) {
                    case Photo:
                        albumFolders = getLocalPhoto();
                        break;
                    case Video:
                        albumFolders = getLocalVideos();
                        break;
                    default:
                        albumFolders = getAllMedias();
                        break;
                }

                return albumFolders;
            }

            @Override
            protected void onPostExecute(List<AlbumFolder> albumFolders) {
                super.onPostExecute(albumFolders);
                if (onScanListener != null && albumFolders != null) {
                    onScanListener.onScanFinish(albumFolders);
                }
            }
        }.execute();
    }

    @WorkerThread
    public ArrayList<AlbumFolder> getLocalPhoto() {
        Map<String, AlbumFolder> albumFolderMap = new HashMap<>();

        String allAlbumName = context.getString(R.string.Chat_All_Image);
        AlbumFolder allFileFolder = new AlbumFolder(allAlbumName);
        allFileFolder.setAlbumFolderType(AlbumFolderType.Photo);

        searchLocalPhoto(albumFolderMap, allFileFolder);

        ArrayList<AlbumFolder> albumFolders = new ArrayList<>();
        for (Map.Entry<String, AlbumFolder> folderEntry : albumFolderMap.entrySet()) {
            AlbumFolder albumFolder = folderEntry.getValue();
            Collections.sort(albumFolder.getAlbumFiles());
            albumFolders.add(albumFolder);
        }

        Collections.sort(allFileFolder.getAlbumFiles());
        albumFolders.add(0, allFileFolder);
        return albumFolders;
    }

    /**
     * Query local image
     *
     * @param albumFolderMap
     */
    @WorkerThread
    public void searchLocalPhoto(Map<String, AlbumFolder> albumFolderMap,AlbumFolder albumFolder) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                IMAGES,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED + " desc");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(IMAGES[0]));

                String imagePath = cursor.getString(cursor.getColumnIndex(IMAGES[1]));
                File file = new File(imagePath);
                if (!file.exists() || !file.canRead() || file.length() < 5 * 1024) continue;

                String name = cursor.getString(cursor.getColumnIndex(IMAGES[2]));
                String title = cursor.getString(cursor.getColumnIndex(IMAGES[3]));
                int bucketId = cursor.getInt(cursor.getColumnIndex(IMAGES[4]));
                String bucketName = cursor.getString(cursor.getColumnIndex(IMAGES[5]));
                String mimeType = cursor.getString(cursor.getColumnIndex(IMAGES[6]));
                long addDate = cursor.getLong(cursor.getColumnIndex(IMAGES[7]));
                long modifyDate = cursor.getLong(cursor.getColumnIndex(IMAGES[8]));
                float latitude = cursor.getFloat(cursor.getColumnIndex(IMAGES[9]));
                float longitude = cursor.getFloat(cursor.getColumnIndex(IMAGES[10]));
                long size = cursor.getLong(cursor.getColumnIndex(IMAGES[11]));

                AlbumFile albumFile = new AlbumFile();
                albumFile.setMediaType(AlbumFile.TYPE_IMAGE);
                albumFile.setId(id);
                albumFile.setPath(imagePath);
                albumFile.setName(name);
                albumFile.setTitle(title);
                albumFile.setBucketId(bucketId);
                albumFile.setBucketName(bucketName);
                albumFile.setMimeType(mimeType);
                albumFile.setAddDate(addDate);
                albumFile.setModifyDate(modifyDate);
                albumFile.setLatitude(latitude);
                albumFile.setLongitude(longitude);
                albumFile.setSize(size);

                albumFolder.addAlbumFile(albumFile);

                AlbumFolder folderInfo = albumFolderMap.get(bucketName);
                if (folderInfo == null) {
                    folderInfo = new AlbumFolder(bucketName);
                    folderInfo.setAlbumFolderType(AlbumFolderType.Photo);
                    folderInfo.addAlbumFile(albumFile);

                    albumFolderMap.put(bucketName, folderInfo);
                } else {
                    folderInfo.addAlbumFile(albumFile);
                }
            }

            cursor.close();
        }
    }

    @WorkerThread
    public ArrayList<AlbumFolder> getLocalVideos() {
        Map<String, AlbumFolder> albumFolderMap = new HashMap<>();

        String allAlbumName = context.getString(R.string.Chat_All_Video);
        AlbumFolder allFileFolder = new AlbumFolder(allAlbumName);
        allFileFolder.setAlbumFolderType(AlbumFolderType.Video);

        searchLocalVideo(albumFolderMap, allFileFolder);

        ArrayList<AlbumFolder> albumFolders = new ArrayList<>();
        for (Map.Entry<String, AlbumFolder> folderEntry : albumFolderMap.entrySet()) {
            AlbumFolder albumFolder = folderEntry.getValue();
            Collections.sort(albumFolder.getAlbumFiles());
            albumFolders.add(albumFolder);
        }

        Collections.sort(allFileFolder.getAlbumFiles());
        albumFolders.add(0, allFileFolder);
        return albumFolders;
    }

    /**
     * query local video
     */
    @WorkerThread
    public void searchLocalVideo(Map<String, AlbumFolder> albumFolderMap,AlbumFolder albumFolder) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                VIDEOS,
                null,
                null,
                MediaStore.Video.Media.DATE_ADDED);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(VIDEOS[0]));
                String path = cursor.getString(cursor.getColumnIndex(VIDEOS[1]));

                File file = new File(path);
                if (!file.exists() || !file.canRead() || file.length() > 10 * 1024 * 1024) continue;

                String name = cursor.getString(cursor.getColumnIndex(VIDEOS[2]));
                String title = cursor.getString(cursor.getColumnIndex(VIDEOS[3]));
                int bucketId = cursor.getInt(cursor.getColumnIndex(VIDEOS[4]));
                String bucketName = cursor.getString(cursor.getColumnIndex(VIDEOS[5]));
                String mimeType = cursor.getString(cursor.getColumnIndex(VIDEOS[6]));
                long addDate = cursor.getLong(cursor.getColumnIndex(VIDEOS[7]));
                long modifyDate = cursor.getLong(cursor.getColumnIndex(VIDEOS[8]));
                float latitude = cursor.getFloat(cursor.getColumnIndex(VIDEOS[9]));
                float longitude = cursor.getFloat(cursor.getColumnIndex(VIDEOS[10]));
                long size = cursor.getLong(cursor.getColumnIndex(VIDEOS[11]));
                long duration = cursor.getLong(cursor.getColumnIndex(VIDEOS[12]));
                String resolution = cursor.getString(cursor.getColumnIndex(VIDEOS[13]));

                AlbumFile videoFile = new AlbumFile();
                videoFile.setMediaType(AlbumFile.TYPE_VIDEO);
                videoFile.setId(id);
                videoFile.setPath(path);
                videoFile.setName(name);
                videoFile.setTitle(title);
                videoFile.setBucketId(bucketId);
                videoFile.setBucketName(bucketName);
                videoFile.setMimeType(mimeType);
                videoFile.setAddDate(addDate);
                videoFile.setModifyDate(modifyDate);
                videoFile.setLatitude(latitude);
                videoFile.setLongitude(longitude);
                videoFile.setSize(size);
                videoFile.setDuration(duration);

                int width = 0, height = 0;
                if (!TextUtils.isEmpty(resolution) && resolution.contains("x")) {
                    String[] resolutionArray = resolution.split("x");
                    width = Integer.valueOf(resolutionArray[0]);
                    height = Integer.valueOf(resolutionArray[1]);
                }
                videoFile.setWidth(width);
                videoFile.setHeight(height);

                albumFolder.addAlbumFile(videoFile);

                AlbumFolder folderInfo = albumFolderMap.get(bucketName);
                if (folderInfo == null) {
                    folderInfo = new AlbumFolder(bucketName);
                    folderInfo.setAlbumFolderType(AlbumFolderType.Photo);
                    folderInfo.addAlbumFile(videoFile);

                    albumFolderMap.put(bucketName, folderInfo);
                } else {
                    folderInfo.addAlbumFile(videoFile);
                }
            }
            cursor.close();
        }
    }

    @WorkerThread
    public ArrayList<AlbumFolder> getAllMedias() {
        Map<String, AlbumFolder> albumFolderMap = new HashMap<>();

        String allAlbumName = context.getString(R.string.Chat_All_Image_Video);
        AlbumFolder allFileFolder = new AlbumFolder(allAlbumName);
        allFileFolder.setAlbumFolderType(AlbumFolderType.All);

        searchLocalPhoto(albumFolderMap, allFileFolder);
        searchLocalVideo(albumFolderMap, allFileFolder);

        ArrayList<AlbumFolder> albumFolders = new ArrayList<>();
        for (Map.Entry<String, AlbumFolder> folderEntry : albumFolderMap.entrySet()) {
            AlbumFolder albumFolder = folderEntry.getValue();
            Collections.sort(albumFolder.getAlbumFiles());
            albumFolders.add(albumFolder);
        }

        Collections.sort(allFileFolder.getAlbumFiles());
        albumFolders.add(0, allFileFolder);
        return albumFolders;
    }
}
