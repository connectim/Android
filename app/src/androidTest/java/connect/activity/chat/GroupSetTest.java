package connect.activity.chat;

import android.app.Instrumentation;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.Espresso;
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
import connect.activity.login.StartActivity;
import connect.utils.TimeUtil;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;

/**
 * Created by pujin on 2017/5/19.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class GroupSetTest {

    @Rule
    public ActivityTestRule<StartActivity> mActivityTestRule = new ActivityTestRule<>(StartActivity.class);

    @Before
    public void startTest() {
        Instrumentation.ActivityMonitor am = new Instrumentation.ActivityMonitor("connect.activity.chat.set.GroupSetActivity", null, false);
        getInstrumentation().addMonitor(am);
        getInstrumentation().waitForMonitor(am);
    }

    @Test
    public void runTest()throws Exception{
        /*ElapsedTimeIdlingResource idlingResource = new ElapsedTimeIdlingResource(1000 * 1);
        Espresso.registerIdlingResources(idlingResource);*/

        groupNameTest();
        myNameTest();
        qrcodeTest();
        managerTest();
        checkTest();
        otherTest();
    }

    public void groupNameTest() {
        ViewInteraction groupNameView = onView(allOf(withId(R.id.groupset_groupname), isDisplayed()));
        groupNameView.perform(click());

        ViewInteraction nameEditView = onView(allOf(withId(R.id.edittxt1), isDisplayed()));
        nameEditView.perform(replaceText("group:" + TimeUtil.getTime(TimeUtil.getCurrentTimeInLong(), TimeUtil.DATE_FORMAT_HOUR_MIN)), click());

        ViewInteraction rightView = onView(allOf(withId(R.id.right_lin), isDisplayed()));
        rightView.perform(click());
    }

    public void myNameTest() {
        ViewInteraction groupNameView = onView(allOf(withId(R.id.groupset_myname), isDisplayed()));
        groupNameView.perform(click());

        ViewInteraction nameEditView = onView(allOf(withId(R.id.edittxt1), isDisplayed()));
        nameEditView.perform(replaceText("nick:" + TimeUtil.getTime(TimeUtil.getCurrentTimeInLong(), TimeUtil.DATE_FORMAT_HOUR_MIN)));

        ViewInteraction rightView = onView(allOf(withId(R.id.right_lin), isDisplayed()));
        rightView.perform(click());
    }

    public void qrcodeTest(){
        ViewInteraction groupNameView = onView(withId(R.id.groupset_qrcode));
        groupNameView.check(matches(isEnabled()));
        groupNameView.perform(click());

        ViewInteraction rightView = onView(allOf(withId(R.id.right_lin), isDisplayed()));
        rightView.perform(click());

        DataInteraction listView = onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(0);
        listView.perform(click());

        ViewInteraction confirmView = onView(allOf(withId(R.id.okBtn), isDisplayed()));
        confirmView.perform(click());

        ViewInteraction leftView = onView(allOf(withId(R.id.left_rela), isDisplayed()));
        leftView.perform(click());
    }

    public void managerTest(){
        ViewInteraction groupNameView = onView(allOf(withId(R.id.groupset_manage),isDisplayed()));
        groupNameView.perform(click());

        ViewInteraction openView = onView(allOf(withId(R.id.toggle),withParent(withId(R.id.groupset_sureinvite)), isDisplayed()));
        openView.perform(click());

        ViewInteraction introduceView = onView(allOf(withId(R.id.groupset_introdue), isDisplayed()));
        introduceView.perform(click());
        ViewInteraction introduceEditView = onView(allOf(withId(R.id.edit), isDisplayed()));
        introduceEditView.perform(replaceText("introduce:" + TimeUtil.getTime(TimeUtil.getCurrentTimeInLong(), TimeUtil.DATE_FORMAT_HOUR_MIN)));
        ViewInteraction rightView = onView(allOf(withId(R.id.right_lin), isDisplayed()));
        rightView.perform(click());

        ViewInteraction transferView = onView(allOf(withId(R.id.groupset_transferto), isDisplayed()));
        transferView.perform(click());
        onData(allOf(withId(R.id.recyclerview),isDisplayed())).perform(actionOnItemAtPosition(0,click()));
        ViewInteraction confirmView = onView(allOf(withId(R.id.okBtn), isDisplayed()));
        confirmView.perform(click());
    }

    public void checkTest() {
        ViewInteraction transferView = onView(allOf(withId(R.id.toggle), withParent(withId(R.id.top)), isDisplayed()));
        transferView.perform(click());

        ViewInteraction muteView = onView(allOf(withId(R.id.toggle), withParent(withId(R.id.mute)), isDisplayed()));
        muteView.perform(click());

        ViewInteraction saveView = onView(allOf(withId(R.id.toggle), withParent(withId(R.id.save)), isDisplayed()));
        saveView.perform(click());
    }

    public void otherTest() {
        ViewInteraction clearView = onView(withId(R.id.clear));
        clearView.perform(click());
        ViewInteraction cancelView = onView(withId(R.id.tv_popup_cancel)).check(matches(isDisplayed()));
        cancelView.perform(click());
        clearView.perform(click());
        DataInteraction confirmView = onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(0);
        confirmView.perform(click());

        ViewInteraction exitView = onView(withId(R.id.delete));
        exitView.perform(click());
        ViewInteraction okView = onView(allOf(withId(R.id.okBtn), isDisplayed()));
        okView.perform(click());
    }

    @After
    public void backTest() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
