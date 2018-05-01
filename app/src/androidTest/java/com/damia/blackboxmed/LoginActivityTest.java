package com.damia.blackboxmed;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.damia.blackboxmed.Activities.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static org.hamcrest.CoreMatchers.not;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> activityRule =
            new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void checkMissingFieldsErrors(){

        //tries to submit
        onView(withId(R.id.loginSendRequest)).perform(click());
        //check if the toast appears with the right error message
        onView(withText("Username is missing")).
                inRoot(withDecorView(not(activityRule.getActivity().getWindow().getDecorView()))).
                check(matches(isDisplayed()));
        //check that the activity doesn't change
        onView(withId(R.id.loginUsername)).check(matches(isDisplayed()));

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e){}

        //fill the username
        onView(withId(R.id.loginUsername)).perform(
                typeText("test"), closeSoftKeyboard());
        //tries to submit
        onView(withId(R.id.loginSendRequest)).perform(click());
        //check if the toast appears with the right error message
        onView(withText("Password is missing")).
                inRoot(withDecorView(not(activityRule.getActivity().getWindow().getDecorView()))).
                check(matches(isDisplayed()));
        //check that the activity doesn't change
        onView(withId(R.id.loginUsername)).check(matches(isDisplayed()));
    }

    @Test
    public void goToRegistrationAndBack(){
        //presses the button to go to the registration
        onView(withId(R.id.loginGoToReg)).perform(click());
        //check that the activity changes
        onView(withId(R.id.regGoToLogin)).check(matches(isDisplayed()));
        //goes back
        onView(withId(R.id.regGoToLogin)).perform(click());
        //check that the activity changes
        onView(withId(R.id.loginGoToReg)).check(matches(isDisplayed()));

    }
}
