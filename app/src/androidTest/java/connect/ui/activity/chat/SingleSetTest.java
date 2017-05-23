package connect.ui.activity.chat;


import android.app.Instrumentation;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import connect.ui.activity.R;
import connect.ui.activity.login.StartActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SingleSetTest {

    @Rule
    public ActivityTestRule<StartActivity> mActivityTestRule = new ActivityTestRule<>(StartActivity.class);

    @Before
    public void startTest() {
        Instrumentation.ActivityMonitor am = new Instrumentation.ActivityMonitor("connect.ui.activity.chat.set.SingleSetActivity", null, false);
        getInstrumentation().addMonitor(am);
        getInstrumentation().waitForMonitor(am);
    }

    @Test
    public void runTest() {
        topTest();
        muteTest();
        clearTest();
    }

    public void topTest() {
        ViewInteraction topView = onView(allOf(withId(R.id.toggle), withParent(withId(R.id.top)), isDisplayed()));
        topView.perform(click());
    }

    public void muteTest() {
        ViewInteraction muteView = onView(allOf(withId(R.id.toggle), withParent(withId(R.id.mute)), isDisplayed()));
        muteView.perform(click());
    }

    public void clearTest() {
        ViewInteraction clearView = onView(withId(R.id.clear));
        clearView.perform(click());
        ViewInteraction cancelView = onView(withId(R.id.tv_popup_cancel)).check(matches(isDisplayed()));
        cancelView.perform(click());

        clearView.perform(click());
        DataInteraction confirmView = onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(0);
        confirmView.perform(click());
    }

    @After
    public void backTest() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction leftView = onView(withId(R.id.left_rela));
        leftView.perform(click());
    }
}
