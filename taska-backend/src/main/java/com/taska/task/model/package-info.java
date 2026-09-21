/**
 * The task vocabulary other modules are allowed to read: entities, enumerations, application results, service parameters, and published events.
 *
 * <p>
 * It depends on none of the module's own sub-packages, which is what keeps the module's internal dependency graph a tree rather than a cycle between
 * services and the types they exchange.
 */
@org.springframework.modulith.NamedInterface("model")
package com.taska.task.model;
