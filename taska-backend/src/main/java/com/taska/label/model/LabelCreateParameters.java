package com.taska.label.model;

/** Application parameters for creating a label. */
public record LabelCreateParameters(String name, String color, int position, boolean favorite) {
}
