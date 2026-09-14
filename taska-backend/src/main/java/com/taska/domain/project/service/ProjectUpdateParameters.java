package com.taska.domain.project.service;

import com.taska.domain.project.ViewStyle;

import java.util.UUID;

/** Application parameters for replacing a project's mutable state. */
public record ProjectUpdateParameters(
        String name,
        String color,
        UUID parentId,
        int position,
        boolean favorite,
        ViewStyle viewStyle,
        UUID planningCalendarId
) {}
