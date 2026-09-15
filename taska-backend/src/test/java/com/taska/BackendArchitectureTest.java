package com.taska;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class BackendArchitectureTest {

  private static final Path DOMAIN_SOURCE = Path.of("src/main/java/com/taska/domain");

  @Test
  void restTransportTypesLiveInControllerPackages() throws IOException {
    List<Path> misplacedTypes;
    try (var paths = Files.walk(DOMAIN_SOURCE)) {
      misplacedTypes =
          paths
              .filter(path -> path.toString().endsWith(".java"))
              .filter(path -> isTransportType(path.getFileName().toString()))
              .filter(path -> !path.toString().contains("/controller/"))
              .toList();
    }

    assertThat(misplacedTypes).isEmpty();
  }

  @Test
  void serviceAndRepositoryPackagesDoNotDependOnTransportTypes() throws IOException {
    List<Path> invalidDependencies;
    try (var paths = Files.walk(DOMAIN_SOURCE)) {
      invalidDependencies =
          paths
              .filter(path -> path.toString().endsWith(".java"))
              .filter(
                  path ->
                      path.toString().contains("/service/")
                          || path.toString().contains("/repository/"))
              .filter(this::importsControllerPackage)
              .toList();
    }

    assertThat(invalidDependencies).isEmpty();
  }

  @Test
  void repositoriesDoNotDependOnServices() throws IOException {
    List<Path> invalidDependencies;
    try (var paths = Files.walk(DOMAIN_SOURCE)) {
      invalidDependencies =
          paths
              .filter(path -> path.toString().endsWith(".java"))
              .filter(path -> path.toString().contains("/repository/"))
              .filter(this::importsServicePackage)
              .toList();
    }

    assertThat(invalidDependencies).isEmpty();
  }

  @Test
  void springComponentsUseFinalDependencyFields() throws IOException {
    List<String> mutableFields;
    try (var paths = Files.walk(DOMAIN_SOURCE)) {
      mutableFields =
          paths
              .filter(path -> path.toString().endsWith(".java"))
              .filter(this::isSpringComponent)
              .flatMap(path -> dependencyFieldViolations(path).stream())
              .toList();
    }

    assertThat(mutableFields).isEmpty();
  }

  @Test
  void httpJsonRejectsPropertiesOutsideRequestContracts() throws IOException {
    String applicationProperties =
        Files.readString(Path.of("src/main/resources/application.properties"));

    assertThat(applicationProperties)
        .contains("spring.jackson.deserialization.fail-on-unknown-properties=true");
  }

  private boolean isTransportType(String fileName) {
    if (fileName.equals("PriorityEvaluationBatchRequest.java")) {
      return false;
    }
    return fileName.endsWith("Controller.java")
        || fileName.endsWith("Dto.java")
        || fileName.endsWith("Request.java")
        || fileName.endsWith("Mapper.java")
        || fileName.endsWith("ExceptionHandler.java");
  }

  private boolean importsControllerPackage(Path path) {
    return fileContains(path, ".controller.");
  }

  private boolean importsServicePackage(Path path) {
    return fileContains(path, ".service.");
  }

  private boolean isSpringComponent(Path path) {
    String source = readSource(path);
    return source.contains("@Service")
        || source.contains("@Component")
        || source.contains("@RestController")
        || source.contains("@ControllerAdvice");
  }

  private List<String> dependencyFieldViolations(Path path) {
    return readSource(path)
        .lines()
        .map(String::trim)
        .filter(line -> line.startsWith("private "))
        .filter(line -> line.endsWith(";"))
        .filter(line -> !line.startsWith("private static "))
        .filter(line -> !line.startsWith("private final "))
        .map(line -> path + ": " + line)
        .toList();
  }

  private boolean fileContains(Path path, String value) {
    return readSource(path).contains(value);
  }

  private String readSource(Path path) {
    try {
      return Files.readString(path);
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to inspect " + path, exception);
    }
  }
}
