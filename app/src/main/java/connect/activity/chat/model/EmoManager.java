package connect.activity.chat.model;

import android.content.res.AssetManager;

import connect.activity.chat.bean.StickerCategory;
import connect.activity.base.BaseApplication;
import connect.utils.FileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmoManager {
    private static EmoManager emojiManager;

    public static EmoManager getInstance() {
        if (emojiManager == null) {
            synchronized (EmoManager.class) {
                if (emojiManager == null) {
                    emojiManager = new EmoManager();
                }
            }
        }
        return emojiManager;
    }

    EmoManager() {
        initEMJOrder();
        initEMJCategories();
    }

    public static final String EMOJI_PATH = "emoji";
    public static final String GIF_PATH = "gif";

    private final static String EMJ_ACTIVITIES = "activities";
    private final static String EMJ_ANIMALS = "animals";
    private final static String EMJ_EMOTION = "emotion";
    private final static String EMJ_FOOD = "food";
    private final static String EMJ_GESTURE = "gestures";
    private final static String EMJ_NATURE = "nature";
    private final static String EMJ_OBJECTS = "Objects";
    private final static String EMJ_PEOPLE = "People";
    private final static String EMJ_PNG = "png";
    private final static String EMJ_TRAVEL = "travel";

    /** Expression package sorting */
    private Map<String, Integer> stickerOrder = new HashMap();
    /** big expression packets */
    private List<String> bigStickers = new ArrayList<>();
    private List<StickerCategory> stickerCategories = new ArrayList<>(10);

    private void initEMJOrder(){
        stickerOrder.put(EMJ_PNG, 0);
        stickerOrder.put(EMJ_ACTIVITIES, 1);
        stickerOrder.put(EMJ_ANIMALS, 2);
        stickerOrder.put(EMJ_EMOTION, 3);
        stickerOrder.put(EMJ_FOOD, 4);
        stickerOrder.put(EMJ_GESTURE, 5);
        stickerOrder.put(EMJ_NATURE, 6);
        stickerOrder.put(EMJ_OBJECTS, 7);
        stickerOrder.put(EMJ_PEOPLE, 8);
        stickerOrder.put(EMJ_TRAVEL, 9);

        bigStickers.add(EMJ_PNG);
    }

    private void initEMJCategories() {
        AssetManager assetManager = BaseApplication.getInstance().getBaseContext().getResources().getAssets();
        try {
            String[] files = assetManager.list(EMOJI_PATH);
            StickerCategory category;
            for (String name : files) {
                if (!FileUtil.hasExtentsion(name)) {
                    if (null == stickerOrder.get(name)) continue;
                    int posi = stickerOrder.get(name);
                    boolean bigStick = bigStickers.contains(name);
                    category = new StickerCategory(name, posi, bigStick);
                    stickerCategories.add(category);
                }
            }

            Collections.sort(stickerCategories, new Comparator<StickerCategory>() {
                @Override
                public int compare(StickerCategory lhs, StickerCategory rhs) {
                    return lhs.getOrder() - rhs.getOrder();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<StickerCategory> getStickerCategories() {
        return stickerCategories;
    }
}