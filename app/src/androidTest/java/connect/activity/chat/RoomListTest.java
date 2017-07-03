package connect.activity.chat;

import android.app.Instrumentation;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import connect.ui.activity.R;
import connect.activity.login.StartActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by pujin on 2017/6/9.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class RoomListTest {

    @Rule
    public ActivityTestRule<StartActivity> mActivityTestRule = new ActivityTestRule<>(StartActivity.class);

    @Before
    public void startTest() {
        Instrumentation.ActivityMonitor am = new Instrumentation.ActivityMonitor("connect.activity.home.HomeActivity", null, false);
        getInstrumentation().addMonitor(am);
        getInstrumentation().waitForMonitor(am);
    }

    @Test
    public void runTest()throws Exception{
        ViewInteraction relativeLayout = onView(
                allOf(withId(R.id.bottom_notify),
                        withParent(withId(R.id.bottom_layout)),
                        isDisplayed()));
        relativeLayout.perform(click());
        ViewInteraction relativeLayout2 = onView(
                allOf(withId(R.id.bottom_notify),
                        withParent(withId(R.id.bottom_layout)),
                        isDisplayed()));
        relativeLayout2.perform(click());

        ViewInteraction relativeLayout3 = onView(
                allOf(withId(R.id.bottom_trash),
                        withParent(withId(R.id.bottom_layout)),
                        isDisplayed()));
        relativeLayout3.perform(click());
    }
}
