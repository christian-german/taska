package com.taska.project.model;

import java.util.UUID;

/** Application parameters for creating a project. */
public record ProjectCreateParameters(String name, String color, UUID parentId, int position, boolean favorite, ViewStyle viewStyle,
        UUID planningCalendarId) {
}
