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

    /**
     * Returns the running application version. The version is read once at startup from the
     * {@code APP_VERSION} environment variable, falling back to {@code "develop"} when unset.
     *
     * @return a single-entry map {@code {"version": "<version string>"}}
     */
    @GetMapping
    public Map<String, String> version() {
        return Map.of("version", VERSION);
    }
}
