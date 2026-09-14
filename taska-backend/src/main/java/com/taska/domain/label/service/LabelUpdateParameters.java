package com.taska.domain.label.service;

/** Application parameters for replacing a label's mutable state. */
public record LabelUpdateParameters(
        String name,
        String color,
        int position,
        boolean favorite
) {}
