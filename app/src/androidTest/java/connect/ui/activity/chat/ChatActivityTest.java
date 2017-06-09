package connect.ui.activity.chat;

import android.app.Instrumentation;
import android.support.test.espresso.Espresso;
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import connect.ui.activity.R;
import connect.ui.activity.login.StartActivity;
import connect.ui.activity.model.ElapsedTimeIdlingResource;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

/**
 * Created by pujin on 2017/6/8.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class ChatActivityTest {

    @Rule
    public ActivityTestRule<StartActivity> mActivityTestRule = new ActivityTestRule<>(StartActivity.class);

    @Before
    public void startTest() {
        Instrumentation.ActivityMonitor am = new Instrumentation.ActivityMonitor("connect.ui.activity.chat.ChatActivity", null, false);
        getInstrumentation().addMonitor(am);
        getInstrumentation().waitForMonitor(am);
    }

    @Test
    public void runTest() {
        ElapsedTimeIdlingResource idlingResource = new ElapsedTimeIdlingResource(1000 * 1);
        Espresso.registerIdlingResources(idlingResource);

        ViewInteraction chatEditText = onView(
                allOf(withId(R.id.inputedit), isDisplayed()));
        chatEditText.perform(replaceText("nxns"), closeSoftKeyboard());

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.inputtxt), withText("发送"),
                        withParent(allOf(withId(R.id.layout_inputbottom),
                                withParent(withId(R.id.layout_bottom)))),
                        isDisplayed()));
        textView3.perform(click());

        ViewInteraction imageView2 = onView(
                allOf(withId(R.id.inputmore), withContentDescription("Connect"),
                        withParent(allOf(withId(R.id.layout_inputbottom),
                                withParent(withId(R.id.layout_bottom)))),
                        isDisplayed()));
        imageView2.perform(click());

        ViewInteraction relativeLayout3 = onView(
                allOf(withClassName(is("android.widget.RelativeLayout")), isDisplayed()));
        relativeLayout3.perform(click());

        ViewInteraction relativeLayout4 = onView(
                allOf(withClassName(is("android.widget.RelativeLayout")), isDisplayed()));
        relativeLayout4.perform(click());

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.photo_library), withText("发送1张图片"), isDisplayed()));
        textView4.perform(click());

        ViewInteraction imageView3 = onView(
                allOf(withId(R.id.inputmore), withContentDescription("Connect"),
                        withParent(allOf(withId(R.id.layout_inputbottom),
                                withParent(withId(R.id.layout_bottom)))),
                        isDisplayed()));
        imageView3.perform(click());

        ViewInteraction relativeLayout5 = onView(
                allOf(withClassName(is("android.widget.RelativeLayout")), isDisplayed()));
        relativeLayout5.perform(click());

        ViewInteraction videoBtnView = onView(
                allOf(withId(R.id.video_btn),
                        withParent(withId(R.id.bottom_rela)),
                        isDisplayed()));
        videoBtnView.perform(click());

        ViewInteraction relativeLayout6 = onView(
                allOf(withId(R.id.send_rela),
                        withParent(withId(R.id.bottom_rela)),
                        isDisplayed()));
        relativeLayout6.perform(click());

        ViewInteraction imageView4 = onView(
                allOf(withId(R.id.inputmore), withContentDescription("Connect"),
                        withParent(allOf(withId(R.id.layout_inputbottom),
                                withParent(withId(R.id.layout_bottom)))),
                        isDisplayed()));
        imageView4.perform(click());

        ViewInteraction relativeLayout7 = onView(
                allOf(withClassName(is("android.widget.RelativeLayout")), isDisplayed()));
        relativeLayout7.perform(click());

        ViewInteraction relativeLayout8 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.listview),
                                withParent(withId(R.id.linearlayout))),
                        3),
                        isDisplayed()));
        relativeLayout8.perform(click());

        ViewInteraction relativeLayout9 = onView(
                allOf(withClassName(is("android.widget.RelativeLayout")), isDisplayed()));
        relativeLayout9.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.recyclerview), isDisplayed()));
        recyclerView.perform(actionOnItemAtPosition(0, click()));
    }


    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
