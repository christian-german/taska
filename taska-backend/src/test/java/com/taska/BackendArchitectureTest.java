package com.taska;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import jakarta.persistence.Entity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

/**
 * Invariants of the in-module layout, which module verification does not reach.
 *
 * <p>
 * {@link ModularityTest} owns everything about who may depend on whom <em>across</em> modules. What is left here is where a type belongs inside its
 * own module, the direction between the task module's subdomains, and two Spring conventions.
 *
 * <p>
 * These rules read bytecode. An earlier version matched file paths and import strings, which could tell where a file sat but never what it actually
 * referenced, and broke on any rename.
 */
class BackendArchitectureTest {

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.taska");

    /**
     * Feature modules, as opposed to the flat application-wide infrastructure modules.
     */
    private static final String[] FEATURE_MODULES = {"com.taska.comment..", "com.taska.label..", "com.taska.notification..",
            "com.taska.planningcalendar..", "com.taska.priority..", "com.taska.project..", "com.taska.task..", "com.taska.version.."};

    /**
     * Types that exist to carry data in or out over a transport. Combined as one predicate rather than chained with {@code or()}, which ArchUnit
     * evaluates left to right and which would therefore escape the feature-module restriction.
     */
    private static final DescribedPredicate<JavaClass> TRANSPORT_TYPE = JavaClass.Predicates.simpleNameEndingWith("Controller")
            .or(JavaClass.Predicates.simpleNameEndingWith("Dto"))
            .or(JavaClass.Predicates.simpleNameEndingWith("Request"))
            .or(JavaClass.Predicates.simpleNameEndingWith("Response"))
            .or(JavaClass.Predicates.simpleNameEndingWith("Mapper"))
            .or(JavaClass.Predicates.simpleNameEndingWith("ExceptionHandler"))
            .as("a transport type");

    private static final String DEFINITION = "com.taska.task.application.definition..";
    private static final String OCCURRENCE = "com.taska.task.application.occurrence..";
    private static final String RECURRENCE = "com.taska.task.application.recurrence..";

    @Test
    void theRootPackageHoldsTheFeaturesAndThePlatform() {
        classes().that()
                .resideOutsideOfPackages(FEATURE_MODULES)
                .and()
                .resideOutsideOfPackage("com.taska.platform..")
                .should()
                .resideInAPackage("com.taska")
                .as("the root package lists the features and the platform; nothing else lives beside them")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void aFeatureModulesSecondLevelIsTheLayerAxis() {
        classes().that()
                .resideInAnyPackage(FEATURE_MODULES)
                .and()
                .haveSimpleNameNotContaining("package-info")
                .should()
                .resideInAnyPackage("com.taska..model..", "com.taska..persistence..", "com.taska..application..", "com.taska..adapter..")
                .as("a feature module's second level is the layer, and no type sits in its root")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void theTechnicalAxisIsNotAlsoAPackageName() {
        noClasses().should()
                .resideInAnyPackage("com.taska..controller..", "com.taska..service..", "com.taska..repository..")
                .as("the technical axis is expressed by adapter / application / persistence")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void jpaEntitiesLiveInModelPackages() {
        classes().that().areAnnotatedWith(Entity.class).should().resideInAPackage("com.taska..model..").check(PRODUCTION_CLASSES);
    }

    @Test
    void repositoriesLiveInPersistencePackages() {
        classes().that()
                .haveSimpleNameEndingWith("Repository")
                .and()
                .resideInAnyPackage(FEATURE_MODULES)
                .should()
                .resideInAPackage("com.taska..persistence..")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void transportTypesLiveInAdapterPackages() {
        classes().that()
                .resideInAnyPackage(FEATURE_MODULES)
                .and(TRANSPORT_TYPE)
                .should()
                .resideInAPackage("com.taska..adapter..")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void onlyAdaptersDependOnAdapters() {
        noClasses().that()
                .resideInAnyPackage(FEATURE_MODULES)
                .and()
                .resideOutsideOfPackage("com.taska..adapter..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("com.taska..adapter..")
                .as("a service needing an outbound call declares a port; the adapter implements it")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void theHttpAdapterDoesNotDependOnOtherTransportAdapters() {
        noClasses().that()
                .resideInAPackage("com.taska..adapter.http..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "com.taska..adapter.mcp..",
                        "com.taska..adapter.openai..",
                        "com.taska..adapter.firebase..",
                        "com.taska..adapter.scheduler..",
                        "com.taska..adapter.events..")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void theInnerLayersDoNotDependOnTheOuterOnes() {
        noClasses().that()
                .resideInAPackage("com.taska..model..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("com.taska..persistence..", "com.taska..application..", "com.taska..adapter..")
                .as("a model depends on no layer of its own module, so the module graph stays a tree")
                .check(PRODUCTION_CLASSES);

        noClasses().that()
                .resideInAPackage("com.taska..persistence..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("com.taska..application..", "com.taska..adapter..")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void taskSubDomainDependenciesFollowOwnershipDirection() {
        noClasses().that()
                .resideInAPackage(DEFINITION)
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(OCCURRENCE, RECURRENCE)
                .as("the definition sub-domain owns neither occurrence behaviour nor recurrence expansion")
                .check(PRODUCTION_CLASSES);

        noClasses().that()
                .resideInAPackage(RECURRENCE)
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(DEFINITION, OCCURRENCE)
                .as("RRULE expansion is a pure function of a series and a period")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void theDefinitionSubDomainDoesNotOwnOccurrencePersistence() {
        noClasses().that()
                .resideInAPackage(DEFINITION)
                .should()
                .dependOnClassesThat()
                .haveSimpleName("TaskOccurrenceStateRepository")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void springComponentsUseFinalDependencyFields() {
        fields().that()
                .areDeclaredInClassesThat()
                .areMetaAnnotatedWith(Component.class)
                .and()
                .areNotStatic()
                .should()
                .beFinal()
                .as("dependencies are constructor-injected and never reassigned")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void httpJsonRejectsPropertiesOutsideRequestContracts() throws IOException {
        String applicationProperties = Files.readString(Path.of("src/main/resources/application.properties"));

        assertThat(applicationProperties).contains("spring.jackson.deserialization.fail-on-unknown-properties=true");
    }
}
