/**
 * Free-text comments attached to a task.
 *
 * <p>
 * The module stores a task identifier but never reads the task itself, so it depends on nothing but the application-wide infrastructure.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Comment", allowedDependencies = {"platform :: config", "platform :: exception"})
package com.taska.comment;
