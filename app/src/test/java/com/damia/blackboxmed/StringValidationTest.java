package com.damia.blackboxmed;

import com.damia.blackboxmed.Activities.RegisterActivity;
import com.damia.blackboxmed.Helper.AddDialogClass;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StringValidationTest {


    @Test
    public void emailSimpleValidator(){
        assertThat(RegisterActivity.isEmailValid("name@email.com"), is(true));
        assertThat(RegisterActivity.isEmailValid("nameemail.com"), is(false));
        assertThat(RegisterActivity.isEmailValid("name@emailcom"), is(false));
    }

    @Test
    public void dateSimpleValidator(){
        assertThat(AddDialogClass.validateDateFormat("2018-12-04"), is(true));
        assertThat(AddDialogClass.validateDateFormat("201812-04"), is(false));
        assertThat(AddDialogClass.validateDateFormat("20181204"), is(false));
        assertThat(AddDialogClass.validateDateFormat("18-12-2004"), is(false));
        assertThat(AddDialogClass.validateDateFormat("2018/12/04"), is(false));
    }

    @Test
    public void timeSimpleValidator(){
        assertThat(AddDialogClass.validateTimeFormat("12:03"), is(true));
        assertThat(AddDialogClass.validateTimeFormat("12.04"), is(false));
        assertThat(AddDialogClass.validateTimeFormat("12,34"), is(false));
        assertThat(AddDialogClass.validateTimeFormat("asd"), is(false));
    }




}
