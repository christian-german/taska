package com.taska;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Verifies the application's module boundaries.
 *
 * <p>
 * {@link ApplicationModules#verify()} rejects a cycle between modules, a dependency a module did not declare in its {@code package-info}, and any
 * reference to a package another module kept internal. It replaces the hand-written path matching this project used before, which could only check
 * where a file sat, not who was allowed to reach it.
 */
class ModularityTest {

    private static final ApplicationModules MODULES = ApplicationModules.of(TaskaApplication.class);

    @Test
    void modulesRespectTheirDeclaredBoundaries() {
        MODULES.verify();
    }

    @Test
    void moduleStructureIsPrintable() {
        // Fails loudly if a module cannot be resolved at all, and documents the layout
        // in the log.
        MODULES.forEach(module -> System.out.println(module.getIdentifier() + "  <-  " + module.getBasePackage()));
    }
}
