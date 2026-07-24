package com.example;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class StatusControllerTest {

    @Test
    public void status_returnsExpectedPayload() {
        Application application = new Application();
        StatusController controller = new StatusController(application);

        Map<String, String> response = controller.status();

        Assert.assertEquals("OK", response.get("status"));
        Assert.assertEquals("Java Maven Application", response.get("app"));
        Assert.assertEquals("Running with a refreshed UI and API", response.get("message"));
    }
}
