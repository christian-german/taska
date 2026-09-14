package com.taska.domain.label.service;

/** Application parameters for creating a label. */
public record LabelCreateParameters(
        String name,
        String color,
        int position,
        boolean favorite
) {}
