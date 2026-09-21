/**
 * The application's own base layer: Spring wiring, the shared error vocabulary, and the helpers every feature's adapters rely on.
 *
 * <p>
 * Grouped here so the root package lists the features and nothing else. Unlike a feature module, it is divided by concern rather than by layer: it
 * has no domain and no use cases, so there are no layers to separate. Each concern is published on its own, so a feature declares exactly the parts
 * of the base layer it uses.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Platform")
package com.taska.platform;
