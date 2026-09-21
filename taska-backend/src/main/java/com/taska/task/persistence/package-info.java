/**
 * Task and occurrence-state repositories.
 *
 * <p>
 * Exposed because the notification sweep and the priority batch both need to query tasks in bulk on their own schedule. Everything else about this
 * module stays internal.
 */
@org.springframework.modulith.NamedInterface("persistence")
package com.taska.task.persistence;
