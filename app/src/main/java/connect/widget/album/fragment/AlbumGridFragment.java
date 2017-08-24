package connect.widget.album.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import connect.ui.activity.R;
import connect.activity.base.BaseFragment;
import connect.utils.ActivityUtil;
import connect.widget.album.AlbumActivity;
import connect.widget.album.adapter.AlbumGridAdp;
import connect.widget.album.model.ImageInfo;

/**
 * All pictures show photo albums
 */
public class AlbumGridFragment extends BaseFragment implements View.OnClickListener{

    private static AlbumGridFragment gridFragment;

    private AlbumActivity activity;
    private GridView gridView;
    private AlbumGridAdp albumGridAdp;

    private TextView titleTxt;
    private TextView noImg;
    private ImageView backImg;
    private TextView sendTxt;
    private TextView albumsTxt;
    private TextView preTxt;

    private List<ImageInfo> imageInfos;

    public static AlbumGridFragment newInstance() {
        if (gridFragment == null) {
            gridFragment = new AlbumGridFragment();
        }
        return gridFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (AlbumActivity) getActivity();
        View rootView = inflater.inflate(R.layout.fragment_album_grid, container, false);
        gridView = (GridView) rootView.findViewById(R.id.gridview);
        backImg= (ImageView) rootView.findViewById(R.id.iv_back);
        titleTxt= (TextView) rootView.findViewById(R.id.tv_dir_title);
        sendTxt= (TextView) rootView.findViewById(R.id.tv_selected_ok);
        noImg= (TextView) rootView.findViewById(R.id.txt2);
        albumsTxt= (TextView) rootView.findViewById(R.id.txt);
        preTxt= (TextView) rootView.findViewById(R.id.txt1);

        albumGridAdp = new AlbumGridAdp();
        gridView.setAdapter(albumGridAdp);
        List<ImageInfo> infos = new ArrayList<>(activity.getImageInfos());
        albumGridAdp.setData(infos);
        albumGridAdp.setAlbumGridListener(new AlbumGridAdp.AlbumGridListener() {

            @Override
            public boolean itemIsSelect(ImageInfo imageInfo) {
                return activity.isSelectImg(imageInfo);
            }

            @Override
            public void ItemClick(int position) {
                activity.getPresenter().galleyFragment(false, position);
            }

            @Override
            public boolean ItemCheck(boolean isChecked, ImageInfo imageInfo) {
                boolean canselect = true;
                if (isChecked && !activity.canSelectImg()) {
                    canselect = false;
                } else {
                    activity.updateSelectInfos(isChecked, imageInfo);
                    refreshSelectedViewState();
                }
                return canselect;
            }
        });
        refreshSelectedViewState();

        backImg.setOnClickListener(this);
        sendTxt.setOnClickListener(this);
        albumsTxt.setOnClickListener(this);
        preTxt.setOnClickListener(this);

        return rootView;
    }

    public void setTitle(String title) {
        titleTxt.setText(title);
        albumsTxt.setText(title);
    }

    public void loadData() {
        if (activity.getImageInfos() != null) {
            imageInfos = new ArrayList<>(activity.getImageInfos());
            albumGridAdp.setData(imageInfos);
        }
    }

    /**
     * Refresh the state of the selected button
     */
    private void refreshSelectedViewState() {
        int selectNum = activity.getSelectSize();
        int maxNum = activity.getMaxSelect();
        if (selectNum == 0) {
            sendTxt.setEnabled(false);
            sendTxt.setText(R.string.Common_OK);

            preTxt.setEnabled(false);
            preTxt.setText(getString(R.string.Chat_Preview));
        } else {
            //Most choose 9 picture
            sendTxt.setEnabled(true);
            sendTxt.setText(getString(R.string.Chat_Select_Count_Max, selectNum, maxNum));

            preTxt.setEnabled(true);
            preTxt.setText(getString(R.string.Chat_Preview_Num, selectNum));
        }
    }

    public void setImageInfos(List<ImageInfo> imageInfos) {
        this.imageInfos = imageInfos;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                ActivityUtil.goBack(activity);
                break;
            case R.id.tv_selected_ok://send
                activity.sendImgInfos();
                break;
            case R.id.txt://select album
                activity.albumFolderDialog();
                break;
            case R.id.txt1://preview
                activity.getPresenter().galleyFragment(true, 0);
                break;
        }
    }
}
