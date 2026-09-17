package com.taska.domain.task.controller;

/** Stable discriminator for task response variants. */
public enum TaskRepresentationKind {
  NON_RECURRING,
  RECURRING_SERIES,
  RECURRING_OCCURRENCE
}
