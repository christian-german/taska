package com.taska.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

@Configuration
@ConfigurationProperties(prefix = "taska")
public class TaskaProperties {
    private final Firebase firebase = new Firebase();
    private final Security security = new Security();
    private final Notification notification = new Notification();
    private final Calendar calendar = new Calendar();

    public static class Firebase {
        /**
         * Indicates whether Firebase should be disabled.
         */
        private boolean disabled = false;

        public boolean isDisabled() {
            return this.disabled;
        }

        public void setDisabled(final boolean disabled) {
            this.disabled = disabled;
        }
    }

    public static class Security {
        /**
         * Indicates whether JWT timeout should be increased by 10 seconds.
         * Spring security by default sets the timeout to 500ms, which is too short for some OIDC servers.
         */
        private boolean increaseTimeout = false;
        /**
         * Indicates whether issuer validation should be disabled.
         * This is useful when the issuer URI is not available or cannot be verified.
         */
        private boolean disableIssuerValidation = false;

        public boolean isIncreaseTimeout() {
            return this.increaseTimeout;
        }

        public boolean isDisableIssuerValidation() {
            return this.disableIssuerValidation;
        }

        public void setIncreaseTimeout(final boolean increaseTimeout) {
            this.increaseTimeout = increaseTimeout;
        }

        public void setDisableIssuerValidation(final boolean disableIssuerValidation) {
            this.disableIssuerValidation = disableIssuerValidation;
        }
    }

    public static class Notification {
        /**
         * Indicates whether notifications are enabled.
         */
        private boolean enabled = true;
        /**
         * Delay in milliseconds between notification checks.
         */
        private long schedulerDelay = 15_000;

        public boolean isEnabled() {
            return this.enabled;
        }

        public long getSchedulerDelay() {
            return this.schedulerDelay;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public void setSchedulerDelay(final long schedulerDelay) {
            this.schedulerDelay = schedulerDelay;
        }
    }

    public static class Calendar {
        /**
         * Time zone used to turn calendar dates into query boundaries.
         */
        private ZoneId timeZone = ZoneId.of("Europe/Paris");

        public ZoneId getTimeZone() {
            return this.timeZone;
        }

        public void setTimeZone(final ZoneId timeZone) {
            this.timeZone = timeZone;
        }
    }

    public Firebase getFirebase() {
        return this.firebase;
    }

    public Security getSecurity() {
        return this.security;
    }

    public Notification getNotification() {
        return this.notification;
    }

    public Calendar getCalendar() {
        return this.calendar;
    }
}
