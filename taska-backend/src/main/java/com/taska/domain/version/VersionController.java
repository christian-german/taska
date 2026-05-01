package com.taska.domain.version;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/version")
public class VersionController {

    private static final String VERSION;

    static {
        String appVersion = System.getenv("APP_VERSION");
        VERSION = (appVersion != null && !appVersion.isBlank()) ? appVersion : "develop";
    }

    @GetMapping
    public Map<String, String> version() {
        return Map.of("version", VERSION);
    }
}
