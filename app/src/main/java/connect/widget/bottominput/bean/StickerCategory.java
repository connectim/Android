package connect.widget.bottominput.bean;

import android.content.Context;
import android.content.res.AssetManager;

import connect.widget.bottominput.EmoManager;
import connect.activity.base.BaseApplication;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StickerCategory implements Serializable {

    /** Big expression shows that the number of each page */
    private final int BIG_COUNT = 10;
    /** Small expression shows that the number of each page */
    private final int SMALL_COUNT = 20;

    /** Expression mapping  Expression == path */
    public static Map<String, String> emojiMaps = new HashMap<>();

    private String name;
    private int order = 0;
    /** show the big expression */
    private boolean isBig;

    private int pagerCount;
    private List<StickPagerBean> stickPagerInfos = new ArrayList<>();

    public StickerCategory(String name, int order, boolean isBig) {
        this.name = name;
        this.order = order;
        this.isBig = isBig;

        loadStickerData();
    }

    /**
     * load all expression
     */
    public void loadStickerData() {
        Context context = BaseApplication.getInstance().getBaseContext();
        AssetManager assetManager = context.getResources().getAssets();
        try {
            String[] files = assetManager.list(EmoManager.EMOJI_PATH + File.separator + name);

            List<String> fileList = Arrays.asList(files);
            for (String str : fileList) {
                emojiMaps.put(str, EmoManager.EMOJI_PATH + File.separator + name + File.separator + str);
            }

            int position = 0;
            StickPagerBean stickPagerBean = null;
            if (isBig()) {
                pagerCount = files.length / BIG_COUNT;
                pagerCount = (int) Math.ceil((double) files.length / BIG_COUNT);
                for (int i = 0; i < fileList.size(); i += BIG_COUNT) {
                    int endpositi = i + BIG_COUNT < fileList.size() ? i + BIG_COUNT : fileList.size();
                    stickPagerBean = new StickPagerBean(true, order, position, pagerCount, name, fileList.subList(i, endpositi));
                    stickPagerInfos.add(stickPagerBean);
                    position++;
                }
            } else {
                pagerCount = (int) Math.ceil((double) files.length / SMALL_COUNT);
                for (int i = 0; i < fileList.size(); i += SMALL_COUNT) {
                    int endpositi = i + SMALL_COUNT < fileList.size() ? i + SMALL_COUNT : fileList.size();
                    stickPagerBean = new StickPagerBean(false, order, position, pagerCount, name, fileList.subList(i, endpositi));
                    stickPagerInfos.add(stickPagerBean);
                    position++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public boolean isBig() {
        return isBig;
    }

    public int getPagerCount() {
        return pagerCount;
    }

    public List<StickPagerBean> getStickPagerInfos() {
        return stickPagerInfos;
    }
}