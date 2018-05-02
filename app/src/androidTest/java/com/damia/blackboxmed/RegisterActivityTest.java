package com.damia.blackboxmed;

import android.support.test.rule.ActivityTestRule;

import com.damia.blackboxmed.Activities.LoginActivity;
import com.damia.blackboxmed.Activities.RegisterActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

public class RegisterActivityTest {

    private String textToBeTyped;

    @Rule
    public ActivityTestRule<RegisterActivity> activityRule =
            new ActivityTestRule<>(RegisterActivity.class);

    @Before
    public void initString(){
        textToBeTyped = "test";
    }

    @Test
    public void checkMissingFieldsErrors(){

        //tries to submit
        onView(withId(R.id.regSendRequest)).perform(click());
        //check if the toast appears with the right error message
        onView(withText("Missing fields")).
                inRoot(withDecorView(not(activityRule.getActivity().getWindow().getDecorView()))).
                check(matches(isDisplayed()));


        try {
            Thread.sleep(1500);
        } catch (InterruptedException e){}

        //fill the username
        onView(withId(R.id.regName)).perform(
                typeText("test"), closeSoftKeyboard());
        //tries to submit
        onView(withId(R.id.regSendRequest)).perform(click());
        //check if the toast appears with the right error message
        onView(withText("Missing fields")).
                inRoot(withDecorView(not(activityRule.getActivity().getWindow().getDecorView()))).
                check(matches(isDisplayed()));

    }

    @Test
    public void goToRegistrationAndBack(){
        //presses the button to go to the registration
        onView(withId(R.id.regGoToLogin)).perform(click());
        //check that the activity changes
        onView(withId(R.id.loginGoToReg)).check(matches(isDisplayed()));
        //goes back
        onView(withId(R.id.loginGoToReg)).perform(click());
        //check that the activity changes
        onView(withId(R.id.regGoToLogin)).check(matches(isDisplayed()));

    }

    @Test
    public void checkEmailFormat(){

        //fills all the fields but put "test" instead of an email in the email field
        onView(withId(R.id.regName)).perform(typeText(textToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.regSurname)).perform(typeText(textToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.regUsername)).perform(typeText(textToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.regPassword)).perform(typeText(textToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.regEmail)).perform(typeText(textToBeTyped), closeSoftKeyboard());

        //tries to submit
        onView(withId(R.id.regSendRequest)).perform(click());

        onView(withText("Email not valid")).
                inRoot(withDecorView(not(activityRule.getActivity().getWindow().getDecorView()))).
                check(matches(isDisplayed()));
    }
}
