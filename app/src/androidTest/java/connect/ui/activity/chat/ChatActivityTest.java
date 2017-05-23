package connect.ui.activity.chat;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import connect.ui.activity.login.StartActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ChatActivityTest {

    @Rule
    public ActivityTestRule<StartActivity> mActivityTestRule = new ActivityTestRule<>(StartActivity.class);

//    @Test
//    public void chatActivityTest() {
//        ViewInteraction textView = onView(
//                allOf(withId(connect.im.R.id.start_message_tv), withText("开始加密聊天"), isDisplayed()));
//        textView.perform(click());
//
//        ViewInteraction textView2 = onView(
//                allOf(withId(connect.im.R.id.backup_local_tv), withText("导入您的本地备份私钥 注册/登录本地账号"), isDisplayed()));
//        textView2.perform(click());
//
//        ViewInteraction linearLayout = onView(
//                allOf(childAtPosition(
//                        allOf(withId(connect.im.R.id.list_view),
//                                withParent(withId(connect.im.R.id.lin_pop_orherlogin))),
//                        0),
//                        isDisplayed()));
//        linearLayout.perform(click());
//
//        ViewInteraction editText = onView(
//                allOf(withId(connect.im.R.id.password_et), isDisplayed()));
//        editText.perform(click());
//
//        ViewInteraction editText2 = onView(
//                allOf(withId(connect.im.R.id.password_et), isDisplayed()));
//        editText2.perform(replaceText("bit123456"), closeSoftKeyboard());
//
//        ViewInteraction button = onView(
//                allOf(withId(connect.im.R.id.next_btn), withText("重置密码登录"), isDisplayed()));
//        button.perform(click());
//
//        ViewInteraction button2 = onView(
//                allOf(withId(connect.im.R.id.next_btn), withText("跳过，开始加密聊天"), isDisplayed()));
//        button2.perform(click());
//
//        ViewInteraction relativeLayout = onView(
//                allOf(withId(connect.im.R.id.contact_rela), isDisplayed()));
//        relativeLayout.perform(click());
//
//        ViewInteraction relativeLayout2 = onView(
//                allOf(withId(connect.im.R.id.content_layout), isDisplayed()));
//        relativeLayout2.perform(click());
//
//        ViewInteraction imageView = onView(
//                allOf(withId(connect.im.R.id.message_img), withContentDescription("Connect")));
//        imageView.perform(scrollTo(), click());
//
//        ViewInteraction linearLayout2 = onView(
//                allOf(withId(connect.im.R.id.right_lin), isDisplayed()));
//        linearLayout2.perform(click());
//
//        ViewInteraction view = onView(
//                allOf(withId(connect.im.R.id.toggle),
//                        withParent(withId(connect.im.R.id.top)),
//                        isDisplayed()));
//        view.perform(click());
//
//        ViewInteraction view2 = onView(
//                allOf(withId(connect.im.R.id.toggle),
//                        withParent(withId(connect.im.R.id.mute)),
//                        isDisplayed()));
//        view2.perform(click());
//
//        ViewInteraction linearLayout3 = onView(
//                allOf(withId(connect.im.R.id.clear), isDisplayed()));
//        linearLayout3.perform(click());
//
//        ViewInteraction textView3 = onView(
//                allOf(withId(connect.im.R.id.tv_popup_cancel), withText("取消"), isDisplayed()));
//        textView3.perform(click());
//
//        ViewInteraction linearLayout4 = onView(
//                allOf(withId(connect.im.R.id.clear), isDisplayed()));
//        linearLayout4.perform(click());
//
//        ViewInteraction linearLayout5 = onView(
//                allOf(childAtPosition(
//                        allOf(withId(connect.im.R.id.list_view),
//                                withParent(withId(connect.im.R.id.lin_pop_orherlogin))),
//                        0),
//                        isDisplayed()));
//        linearLayout5.perform(click());
//
//        ViewInteraction textView4 = onView(
//                withId(connect.im.R.id.name));
//        textView4.perform(scrollTo(), click());
//
//        ViewInteraction recyclerView = onView(
//                allOf(withId(connect.im.R.id.recyclerview), isDisplayed()));
//        recyclerView.perform(actionOnItemAtPosition(1, click()));
//
//        ViewInteraction recyclerView2 = onView(
//                allOf(withId(connect.im.R.id.recyclerview), isDisplayed()));
//        recyclerView2.perform(actionOnItemAtPosition(0, click()));
//
//        ViewInteraction linearLayout6 = onView(
//                allOf(withId(connect.im.R.id.right_lin), isDisplayed()));
//        linearLayout6.perform(click());
//
//        ViewInteraction linearLayout7 = onView(
//                allOf(withId(connect.im.R.id.right_lin), isDisplayed()));
//        linearLayout7.perform(click());
//
//        ViewInteraction relativeLayout3 = onView(
//                withId(connect.im.R.id.groupset_groupname));
//        relativeLayout3.perform(scrollTo(), click());
//
//        ViewInteraction editText3 = onView(
//                allOf(withId(connect.im.R.id.edittxt1), withText("Leno的朋友"), isDisplayed()));
//        editText3.perform(click());
//
//        ViewInteraction editText4 = onView(
//                allOf(withId(connect.im.R.id.edittxt1), withText("Leno的朋友"), isDisplayed()));
//        editText4.perform(replaceText("Leno的"), closeSoftKeyboard());
//
//        ViewInteraction linearLayout8 = onView(
//                allOf(withId(connect.im.R.id.right_lin), isDisplayed()));
//        linearLayout8.perform(click());
//
//        ViewInteraction relativeLayout4 = onView(
//                withId(connect.im.R.id.groupset_myname));
//        relativeLayout4.perform(scrollTo(), click());
//
//        ViewInteraction editText5 = onView(
//                allOf(withId(connect.im.R.id.edittxt1), withText("Leno"), isDisplayed()));
//        editText5.perform(click());
//
//        ViewInteraction editText6 = onView(
//                allOf(withId(connect.im.R.id.edittxt1), withText("Leno"), isDisplayed()));
//        editText6.perform(replaceText("Le"), closeSoftKeyboard());
//
//        ViewInteraction linearLayout9 = onView(
//                allOf(withId(connect.im.R.id.right_lin), isDisplayed()));
//        linearLayout9.perform(click());
//
//        ViewInteraction relativeLayout5 = onView(
//                withId(connect.im.R.id.groupset_qrcode));
//        relativeLayout5.perform(scrollTo(), click());
//
//        ViewInteraction linearLayout10 = onView(
//                allOf(withId(connect.im.R.id.right_lin), isDisplayed()));
//        linearLayout10.perform(click());
//
//        ViewInteraction linearLayout11 = onView(
//                allOf(childAtPosition(
//                        allOf(withId(connect.im.R.id.list_view),
//                                withParent(withId(connect.im.R.id.lin_pop_orherlogin))),
//                        0),
//                        isDisplayed()));
//        linearLayout11.perform(click());
//
//        ViewInteraction textView5 = onView(
//                allOf(withId(connect.im.R.id.okBtn), withText("确定"),
//                        withParent(withId(connect.im.R.id.cancel_ok_re)),
//                        isDisplayed()));
//        textView5.perform(click());
//
//        ViewInteraction linearLayout12 = onView(
//                allOf(withId(connect.im.R.id.right_lin), isDisplayed()));
//        linearLayout12.perform(click());
//
//        ViewInteraction linearLayout13 = onView(
//                allOf(childAtPosition(
//                        allOf(withId(connect.im.R.id.list_view),
//                                withParent(withId(connect.im.R.id.lin_pop_orherlogin))),
//                        0),
//                        isDisplayed()));
//        linearLayout13.perform(click());
//
//        ViewInteraction textView6 = onView(
//                allOf(withId(connect.im.R.id.cancelBtn), withText("取消"),
//                        withParent(withId(connect.im.R.id.cancel_ok_re)),
//                        isDisplayed()));
//        textView6.perform(click());
//
//        ViewInteraction linearLayout14 = onView(
//                allOf(withId(connect.im.R.id.right_lin), isDisplayed()));
//        linearLayout14.perform(click());
//
//        ViewInteraction textView7 = onView(
//                allOf(withId(connect.im.R.id.tv_popup_cancel), withText("取消"), isDisplayed()));
//        textView7.perform(click());
//
//        ViewInteraction relativeLayout6 = onView(
//                allOf(withId(connect.im.R.id.left_rela), isDisplayed()));
//        relativeLayout6.perform(click());
//
//        ViewInteraction relativeLayout7 = onView(
//                withId(connect.im.R.id.groupset_manage));
//        relativeLayout7.perform(scrollTo(), click());
//
//        ViewInteraction view3 = onView(
//                allOf(withId(connect.im.R.id.toggle),
//                        withParent(withId(connect.im.R.id.groupset_sureinvite)),
//                        isDisplayed()));
//        view3.perform(click());
//
//        ViewInteraction view4 = onView(
//                allOf(withId(connect.im.R.id.toggle),
//                        withParent(withId(connect.im.R.id.groupset_sureinvite)),
//                        isDisplayed()));
//        view4.perform(click());
//
//        ViewInteraction relativeLayout8 = onView(
//                allOf(withId(connect.im.R.id.groupset_introdue), isDisplayed()));
//        relativeLayout8.perform(click());
//
//        ViewInteraction editText7 = onView(
//                allOf(withId(connect.im.R.id.edit), withText("Leno的"), isDisplayed()));
//        editText7.perform(click());
//
//        ViewInteraction editText8 = onView(
//                allOf(withId(connect.im.R.id.edit), withText("Leno的"), isDisplayed()));
//        editText8.perform(replaceText("Leno的哦SOS"), closeSoftKeyboard());
//
//        ViewInteraction linearLayout15 = onView(
//                allOf(withId(connect.im.R.id.right_lin), isDisplayed()));
//        linearLayout15.perform(click());
//
//        ViewInteraction relativeLayout9 = onView(
//                allOf(withId(connect.im.R.id.groupset_transferto), isDisplayed()));
//        relativeLayout9.perform(click());
//
//        ViewInteraction recyclerView3 = onView(
//                allOf(withId(connect.im.R.id.recyclerview), isDisplayed()));
//        recyclerView3.perform(actionOnItemAtPosition(0, click()));
//
//        ViewInteraction textView8 = onView(
//                allOf(withId(connect.im.R.id.cancelBtn), withText("取消"),
//                        withParent(withId(connect.im.R.id.cancel_ok_re)),
//                        isDisplayed()));
//        textView8.perform(click());
//
//        ViewInteraction relativeLayout10 = onView(
//                allOf(withId(connect.im.R.id.left_rela), isDisplayed()));
//        relativeLayout10.perform(click());
//
//        ViewInteraction relativeLayout11 = onView(
//                allOf(withId(connect.im.R.id.left_rela), isDisplayed()));
//        relativeLayout11.perform(click());
//
//        ViewInteraction view5 = onView(
//                allOf(withId(connect.im.R.id.toggle),
//                        withParent(withId(connect.im.R.id.top))));
//        view5.perform(scrollTo(), click());
//
//        ViewInteraction view6 = onView(
//                allOf(withId(connect.im.R.id.toggle),
//                        withParent(withId(connect.im.R.id.top))));
//        view6.perform(scrollTo(), click());
//
//        ViewInteraction view7 = onView(
//                allOf(withId(connect.im.R.id.toggle),
//                        withParent(withId(connect.im.R.id.mute))));
//        view7.perform(scrollTo(), click());
//
//        ViewInteraction view8 = onView(
//                allOf(withId(connect.im.R.id.toggle),
//                        withParent(withId(connect.im.R.id.mute))));
//        view8.perform(scrollTo(), click());
//
//        ViewInteraction view9 = onView(
//                allOf(withId(connect.im.R.id.toggle),
//                        withParent(withId(connect.im.R.id.save))));
//        view9.perform(scrollTo(), click());
//
//        ViewInteraction view10 = onView(
//                allOf(withId(connect.im.R.id.toggle),
//                        withParent(withId(connect.im.R.id.save))));
//        view10.perform(scrollTo(), click());
//
//        ViewInteraction linearLayout16 = onView(
//                withId(connect.im.R.id.clear));
//        linearLayout16.perform(scrollTo(), click());
//
//        ViewInteraction linearLayout17 = onView(
//                allOf(childAtPosition(
//                        allOf(withId(connect.im.R.id.list_view),
//                                withParent(withId(connect.im.R.id.lin_pop_orherlogin))),
//                        0),
//                        isDisplayed()));
//        linearLayout17.perform(click());
//
//        ViewInteraction textView9 = onView(
//                withId(connect.im.R.id.name));
//        textView9.perform(scrollTo(), click());
//
//        ViewInteraction recyclerView4 = onView(
//                allOf(withId(connect.im.R.id.recyclerview), isDisplayed()));
//        recyclerView4.perform(actionOnItemAtPosition(0, click()));
//
//        ViewInteraction linearLayout18 = onView(
//                allOf(withId(connect.im.R.id.right_lin), isDisplayed()));
//        linearLayout18.perform(click());
//
//        ViewInteraction relativeLayout12 = onView(
//                allOf(withId(connect.im.R.id.left_rela), isDisplayed()));
//        relativeLayout12.perform(click());
//
//    }

//    private static Matcher<View> childAtPosition(
//            final Matcher<View> parentMatcher, final int position) {
//
//        return new TypeSafeMatcher<View>() {
//            @Override
//            public void describeTo(Description description) {
//                description.appendText("Child at position " + position + " in parent ");
//                parentMatcher.describeTo(description);
//            }
//
//            @Override
//            public boolean matchesSafely(View view) {
//                ViewParent parent = view.getParent();
//                return parent instanceof ViewGroup && parentMatcher.matches(parent)
//                        && view.equals(((ViewGroup) parent).getChildAt(position));
//            }
//        };
//    }
}
