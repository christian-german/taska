/**
 * Weekly availability windows constraining when work may be scheduled.
 *
 * <p>
 * Other modules ask this one whether an instant is allowed; it never looks at what they are trying to schedule.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Planning calendar", allowedDependencies = {"platform :: config",
        "platform :: exception"})
package com.taska.planningcalendar;
