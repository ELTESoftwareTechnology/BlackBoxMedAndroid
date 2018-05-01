package com.damia.blackboxmed;

import com.damia.blackboxmed.Activities.RegisterActivity;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EmailValidateTest {

    @Test
    public void emailValidator_CorrectEmailSimple_ReturnsTrue(){
        assertThat(RegisterActivity.isEmailValid("name@email.com"), is(true));
    }


}
