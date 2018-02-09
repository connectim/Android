package connect.widget.album.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import connect.activity.chat.exts.VideoPlayerActivity;
import connect.ui.activity.R;
import connect.widget.album.AlbumActivity;
import connect.widget.album.adapter.AlbumPreviewAdapter;
import connect.widget.album.model.AlbumFile;
import connect.widget.album.view.HackyViewPager;

public class AlbumPreviewFragment extends Fragment implements View.OnClickListener{

    private AlbumActivity activity;
    private HackyViewPager viewPager;
    private ImageView videoImg;
    private ImageView backImg;
    private TextView titleTxt;
    private TextView sendTxt;
    private CheckBox checkBox;
    private AlbumPreviewAdapter galleryAdp;

    /** Preview status true: preview false: view selected pictures */
    private boolean preViewState = false;
    /** Preview loaded image location */
    private int previewPosition;
    private List<AlbumFile> albumFiles;

    public static AlbumPreviewFragment newInstance() {
        return new AlbumPreviewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (AlbumActivity) getActivity();
        View rootView = inflater.inflate(R.layout.fragment_album_gallery, container, false);

        viewPager = (HackyViewPager) rootView.findViewById(R.id.gallery);
        videoImg= (ImageView) rootView.findViewById(R.id.img2);
        backImg = (ImageView) rootView.findViewById(R.id.iv_back);
        titleTxt = (TextView) rootView.findViewById(R.id.tv_title);
        sendTxt = (TextView) rootView.findViewById(R.id.txt);
        checkBox = (CheckBox) rootView.findViewById(R.id.checkbox);

        backImg.setOnClickListener(this);
        titleTxt.setOnClickListener(this);
        sendTxt.setOnClickListener(this);
        videoImg.setOnClickListener(this);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int currentPosition = viewPager.getCurrentItem();
                AlbumFile albumFile = albumFiles.get(currentPosition);
                boolean selectstate = !albumFile.isChecked();
                if (selectstate && !activity.canSelectImg()) {
                    return;
                }

                activity.updateSelectInfos(selectstate, albumFiles.get(currentPosition));
                refreshSelectedViewState(currentPosition);
            }
        });

        galleryAdp = new AlbumPreviewAdapter();
        viewPager.setAdapter(galleryAdp);
        initImgInfos();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                refreshSelectedViewState(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        return rootView;
    }

    public void initImgInfos(){
        List<AlbumFile> infos = preViewState ? activity.getSelectInfoList() : activity.getAlbumFiles();
        albumFiles = new ArrayList<>(infos);
        galleryAdp.setData(albumFiles);
        if (albumFiles.size() >= previewPosition) {
            viewPager.setCurrentItem(previewPosition, false);//Cancel the animation
            refreshSelectedViewState(previewPosition);
        }
    }

    public void setPreViewState(boolean prestate, int posi) {
        this.preViewState = prestate;
        this.previewPosition = posi;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            initImgInfos();
        }
    }

    public void refreshSelectedViewState(int position) {
        String title = String.format(Locale.ENGLISH, "%1$d/%2$d", position + 1, albumFiles.size());
        titleTxt.setText(title);

        int selectNum = activity.getSelectSize();
        if (selectNum == 0) {
            sendTxt.setText(getString(R.string.Common_OK));
        } else {
            sendTxt.setText(getString(R.string.Chat_Select_Count, selectNum));
        }

        AlbumFile albumFile = albumFiles.get(position);
        boolean isChecked = albumFile.isChecked();
        checkBox.setSelected(isChecked);
        if (albumFile.getMediaType() == AlbumFile.TYPE_IMAGE) {
            videoImg.setVisibility(View.GONE);
        } else {
            videoImg.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                activity.getPresenter().gridAlbumFragment();
                break;
            case R.id.txt:
                if (activity.getSelectSize() == 0) {
                    int currentPosition = viewPager.getCurrentItem();
                    AlbumFile albumFile = albumFiles.get(currentPosition);
                    activity.updateSelectInfos(true, albumFile);
                }
                activity.sendImgInfos();
                break;
            case R.id.img2:
                AlbumFile albumFile = (AlbumFile) v.getTag();
                int videolength = (int) (albumFile.getDuration() / 1000);
                String filepath = albumFile.getPath();
                String length = String.valueOf(videolength);
                VideoPlayerActivity.startActivity(activity, filepath, length, null);
                break;
        }
    }
}
