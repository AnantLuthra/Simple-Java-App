package com.example;

import org.junit.Assert;
import org.junit.Test;

public class ApplicationTest {

    @Test
    public void getStatus_returnsOk() {
        Application application = new Application();

        Assert.assertEquals("OK", application.getStatus());
    }
}
