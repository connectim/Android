package connect.widget.bottominput;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;

import connect.utils.RegularUtil;
import connect.utils.data.ResourceUtil;
import connect.widget.bottominput.bean.StickerCategory;
import connect.activity.base.BaseApplication;
import connect.utils.FileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmoManager {
    public static EmoManager emojiManager = getInstance();

    public synchronized static EmoManager getInstance() {
        if (emojiManager == null) {
            emojiManager = new EmoManager();
        }
        return emojiManager;
    }

    public static final String EMOJI_PATH = "emoji";
    public static final String GIF_PATH = "gif";

    private final static String EMJ_EMOTION = "emotion";
    private final static String EMJ_PNG = "png";

    /** Expression package sorting */
    private Map<String, Integer> stickerOrder = new HashMap();
    /** big expression packets */
    private List<String> bigStickers = new ArrayList<>();
    private List<StickerCategory> stickerCategories = new ArrayList<>(10);
    private StickerCategoryCompara categoryCompara = new StickerCategoryCompara();

    public void initEmojiResource(){
        initEMJOrder();
        initEMJCategories();
    }

    private void initEMJOrder(){
        stickerOrder.put(EMJ_PNG, 0);
        stickerOrder.put(EMJ_EMOTION, 1);

        bigStickers.add(EMJ_PNG);
    }

    private void initEMJCategories() {
        Context context = BaseApplication.getInstance().getBaseContext();
        AssetManager assetManager = context.getResources().getAssets();
        try {
            String[] files = assetManager.list(EMOJI_PATH);
            for (String name : files) {
                if (!FileUtil.hasExtentsion(name)) {
                    if (null != stickerOrder.get(name)) {
                        int posi = stickerOrder.get(name);
                        boolean bigStick = bigStickers.contains(name);
                        StickerCategory category = new StickerCategory(name, posi, bigStick);
                        stickerCategories.add(category);
                    }
                }
            }

            Collections.sort(stickerCategories, categoryCompara);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<StickerCategory> getStickerCategories() {
        return stickerCategories;
    }

    /**
     * expression to text
     *
     * @param content
     * @return
     */
    public SpannableStringBuilder txtTransEmotion(String content) {
        SpannableStringBuilder mSpannableString = new SpannableStringBuilder(content);
        Matcher emjMatcher = Pattern.compile(RegularUtil.VERIFYCATION_EMJ).matcher(content);
        while (emjMatcher.find()) {
            int start = emjMatcher.start();
            int end = emjMatcher.end();
            String emot = content.substring(start, end);
            emot = emot.substring(1, emot.length() - 1) + ".png";
            String key = StickerCategory.emojiMaps.get(emot);
            if (!TextUtils.isEmpty(key)) {
                emot = key;
                Drawable d = ResourceUtil.getEmotDrawable(emot);
                if (d != null) {
                    ImageSpan span = new ImageSpan(d);
                    mSpannableString.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return mSpannableString;
    }

    private class StickerCategoryCompara implements Comparator<StickerCategory> {

        @Override
        public int compare(StickerCategory lhs, StickerCategory rhs) {
            return lhs.getOrder() - rhs.getOrder();
        }
    }
}