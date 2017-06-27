package connect.widget.album.ui.activity;


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

import connect.ui.activity.R;
import connect.activity.chat.exts.VideoPlayerActivity;
import connect.widget.album.adapter.AlbumGalleryAdp;
import connect.widget.album.entity.ImageInfo;
import connect.widget.album.ui.widget.HackyViewPager;

public class AlbumGalleryFragment extends Fragment implements View.OnClickListener{

    private static AlbumGalleryFragment galleryFragment;

    private PhotoAlbumActivity activity;
    private HackyViewPager viewPager;
    private ImageView videoImg;
    private ImageView backImg;
    private TextView titleTxt;
    private TextView sendTxt;
    private CheckBox checkBox;
    private AlbumGalleryAdp galleryAdp;

    /** Preview status true: preview false: view selected pictures */
    private boolean preViewState = false;
    /** Preview loaded image location */
    private int previewPosition;
    private List<ImageInfo> imageInfos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (PhotoAlbumActivity) getActivity();
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
                ImageInfo imageInfo = imageInfos.get(currentPosition);
                boolean selectstate = !activity.isSelectImg(imageInfo);
                if (selectstate && !activity.canSelectImg()) {
                    return;
                }

                activity.updateSelectInfos(selectstate, imageInfos.get(currentPosition));
                refreshSelectedViewState(currentPosition);
            }
        });

        galleryAdp = new AlbumGalleryAdp();
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

    public static AlbumGalleryFragment newInstance() {
        if (galleryFragment == null) {
            galleryFragment = new AlbumGalleryFragment();
        }
        return galleryFragment;
    }

    public void initImgInfos(){
        List<ImageInfo> infos = preViewState ? activity.getSelectInfoList() : activity.getImageInfoList();
        imageInfos = new ArrayList<>(infos);
        galleryAdp.setData(imageInfos);
        viewPager.setCurrentItem(previewPosition, false);//Cancel the animation
        refreshSelectedViewState(previewPosition);
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
        String title = String.format(Locale.ENGLISH, "%1$d/%2$d", position + 1, imageInfos.size());
        titleTxt.setText(title);

        int selectNum = activity.getSelectSize();
        if (selectNum == 0) {
            sendTxt.setText(getString(R.string.Common_OK));
        } else {
            sendTxt.setText(getString(R.string.Chat_Select_Count, selectNum));
        }

        ImageInfo imageInfo = imageInfos.get(position);
        boolean isChecked = activity.isSelectImg(imageInfo);
        checkBox.setSelected(isChecked);
        if (imageInfo.getFileType() == 0) {
            videoImg.setVisibility(View.GONE);
        } else {
            videoImg.setVisibility(View.VISIBLE);
            videoImg.setTag(imageInfo);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                activity.gridListInfos();
                break;
            case R.id.txt:
                if (activity.getSelectSize() == 0) {
                    int currentPosition = viewPager.getCurrentItem();
                    ImageInfo imageInfo = imageInfos.get(currentPosition);
                    activity.updateSelectInfos(true, imageInfo);
                }
                activity.sendImgInfos();
                break;
            case R.id.img2:
                ImageInfo imageInfo = (ImageInfo) v.getTag();
                int length = (int) (imageInfo.getImageFile().getVideoLength() / 1000);
                VideoPlayerActivity.startActivity(activity, imageInfo.getImageFile().getAbsolutePath(), length);
                break;
        }
    }
}
