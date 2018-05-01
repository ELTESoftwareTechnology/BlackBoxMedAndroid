package com.damia.blackboxmed;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.damia.blackboxmed.Activities.HomeActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class HomeActivityTest_add_measure {

    private String textToBeTyped;
    private String dateToBeTyped;
    private String timeToBeTyped;
    private String intToBeTyped;

    @Rule
    public ActivityTestRule<HomeActivity> activityRule =
            new ActivityTestRule<>(HomeActivity.class);

    @Before
    public void initStrings(){
        textToBeTyped = "test";
        intToBeTyped = "1";
        dateToBeTyped = "2018-05-01";
        timeToBeTyped = "12:00";
    }


    @Test
    public void pressAddButton() {
        //check if the dialog is displayed
        onView(withId(R.id.btnAddMeasure)).perform(click());
        onView(withId(R.id.dialog_layout)).check(matches(isDisplayed()));

    }

    @Test
    public void editFields() {

        //show the dialog
        onView(withId(R.id.btnAddMeasure)).perform(click());

        //properly edit all the fields
        onView(withId(R.id.in_type)).perform(typeText(textToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.in_value)).perform(typeText(intToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.in_units)).perform(typeText(textToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.in_date)).perform(typeText(dateToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.in_time)).perform(typeText(timeToBeTyped), closeSoftKeyboard());

        //check that the values where written correcly
        onView(withId(R.id.in_type)).check(matches(withText(textToBeTyped)));
        onView(withId(R.id.in_units)).check(matches(withText(textToBeTyped)));
        onView(withId(R.id.in_value)).check(matches(withText(intToBeTyped)));
        onView(withId(R.id.in_time)).check(matches(withText(timeToBeTyped)));
        onView(withId(R.id.in_date)).check(matches(withText(dateToBeTyped)));

        //adds the measure
        onView(withId(R.id.in_add)).perform(click());
        onView(withId(R.id.dialog_layout)).check(doesNotExist());
    }

    @Test
    public void showErrorOnMissingFields() {

        //show the dialog
        onView(withId(R.id.btnAddMeasure)).perform(click());

        //fills just one field
        onView(withId(R.id.in_type)).perform(typeText(textToBeTyped), closeSoftKeyboard());
        //tries to submit
        onView(withId(R.id.in_add)).perform(click());
        //check the error message and that the dialog is still open
        onView(withId(R.id.dialog_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.in_err)).check(matches(withText("Please fill all the fields")));


    }

    @Test
    public void showErrorOnInputType() {

        //show the dialog
        onView(withId(R.id.btnAddMeasure)).perform(click());
        //fills all the fields but put "test" instead of a number in the value field
        onView(withId(R.id.in_type)).perform(typeText(textToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.in_value)).perform(typeText(textToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.in_units)).perform(typeText(textToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.in_date)).perform(typeText(dateToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.in_time)).perform(typeText(timeToBeTyped), closeSoftKeyboard());

        //tries to submit
        onView(withId(R.id.in_add)).perform(click());

        //check the error message and that the dialog is still open
        onView(withId(R.id.dialog_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.in_err)).check(matches(withText("Value input should be a number")));
    }

    @Test
    public void showErrorOnDateAndTimeFormat(){
        //show the dialog
        onView(withId(R.id.btnAddMeasure)).perform(click());

        //fills all the fields but put "test" instead of a a correct date or time
        onView(withId(R.id.in_type)).perform(typeText(textToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.in_value)).perform(typeText(intToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.in_units)).perform(typeText(textToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.in_date)).perform(typeText(textToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.in_time)).perform(typeText(timeToBeTyped), closeSoftKeyboard());

        //tries to submit
        onView(withId(R.id.in_add)).perform(click());

        //check the error message and that the dialog is still open
        onView(withId(R.id.dialog_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.in_err)).check(matches(withText("Invalid date format, yyyy-mm-dd")));

        //fixes the date but changes the time to a "test"
        onView(withId(R.id.in_time)).perform(clearText());
        onView(withId(R.id.in_time)).perform(typeText(textToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.in_date)).perform(clearText());
        onView(withId(R.id.in_date)).perform(typeText(dateToBeTyped), closeSoftKeyboard());

        //tries to submit
        onView(withId(R.id.in_add)).perform(click());

        //check the error message and that the dialog is still open
        onView(withId(R.id.dialog_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.in_err)).check(matches(withText("Invalid time format, HH:mm")));
    }
}
