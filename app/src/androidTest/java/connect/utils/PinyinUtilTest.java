package connect.utils;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by Administrator on 2017/7/10.
 */
public class PinyinUtilTest {

    @Test
    public void chatToPinyinTest() throws Exception {
        String pinyin = PinyinUtil.chatToPinyin('a');
        assertTrue(pinyin.length() > 0);
    }

    @Test
    public void isChineseTest() throws Exception {
        assertTrue(PinyinUtil.isChinese("什"));
    }

    @Test
    public void isEnglishTest() throws Exception {
        assertTrue(PinyinUtil.isEnglish("b"));
    }

    @Test
    public void getFirstCharTest() throws Exception {
        assertTrue(PinyinUtil.getFirstChar("bc").length() > 0);
    }

}