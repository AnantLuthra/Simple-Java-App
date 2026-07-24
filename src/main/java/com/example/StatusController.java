package com.example;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    private final Application application;

    public StatusController(Application application) {
        this.application = application;
    }

    @GetMapping("/api/status")
    public Map<String, String> status() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", application.getStatus());
        response.put("app", "Java Maven Application");
        response.put("message", "Running with a refreshed UI and API");
        return response;
    }
}
