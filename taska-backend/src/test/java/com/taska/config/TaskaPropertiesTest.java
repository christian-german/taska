package com.taska.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.time.ZoneId;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TaskaPropertiesTest {

    @Test
    void bindsCalendarTimeZoneAsZoneId() {
        TaskaProperties properties = new Binder(new MapConfigurationPropertySource(Map.of(
                "taska.calendar.time-zone", "America/New_York")))
                .bind("taska", Bindable.of(TaskaProperties.class))
                .orElseThrow(() -> new AssertionError("Taska properties did not bind"));

        assertThat(properties.getCalendar().getTimeZone()).isEqualTo(ZoneId.of("America/New_York"));
    }
}
