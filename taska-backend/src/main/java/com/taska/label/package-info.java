/**
 * User-defined labels attachable to tasks.
 *
 * <p>
 * The {@code task_labels} join table is owned by the task module; a label knows nothing about the tasks carrying it.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Label", allowedDependencies = {"platform :: config", "platform :: exception"})
package com.taska.label;
