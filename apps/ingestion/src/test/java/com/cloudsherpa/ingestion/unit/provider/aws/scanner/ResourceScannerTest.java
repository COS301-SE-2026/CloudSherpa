package com.cloudsherpa.ingestion.unit.provider.aws.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.cloudsherpa.ingestion.provider.scanner.ResourceScanner;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ResourceScannerTest {

  private List<ResourceScanner> scanners;

  @BeforeEach
  void setUp() {
    scanners = discoverScanners();
  }

  @Test
  void shouldDiscoverResourceScanners() {
    assertFalse(scanners.isEmpty(), "No concrete ResourceScanner implementations were discovered");
  }

  @Test
  void everyScanner_shouldHaveProvider() {
    scanners.forEach(
        scanner -> {
          assertNotNull(
              scanner.getProvider(), scanner.getClass().getSimpleName() + " has a null provider");

          assertFalse(
              scanner.getProvider().isBlank(),
              scanner.getClass().getSimpleName() + " has a blank provider");
        });
  }

  @Test
  void everyScanner_shouldHaveServiceName() {
    scanners.forEach(
        scanner -> {
          assertNotNull(
              scanner.getServiceName(),
              scanner.getClass().getSimpleName() + " has a null service name");

          assertFalse(
              scanner.getServiceName().isBlank(),
              scanner.getClass().getSimpleName() + " has a blank service name");
        });
  }

  @Test
  void everyScanner_shouldReturnNonNullPermissions() {
    scanners.forEach(
        scanner ->
            assertNotNull(
                scanner.getPermissionsRequired(),
                scanner.getClass().getSimpleName() + " returned null permissions"));
  }

  @Test
  void everyScanner_shouldHaveUniqueServiceNames() {
    Set<String> serviceNames =
        scanners.stream().map(ResourceScanner::getServiceName).collect(Collectors.toSet());

    assertEquals(
        scanners.size(), serviceNames.size(), "Duplicate ResourceScanner service names detected");
  }

  @Test
  void everyScanner_shouldHaveUniqueProviderAndServiceCombination() {
    Set<String> identifiers =
        scanners.stream()
            .map(scanner -> scanner.getProvider() + ":" + scanner.getServiceName())
            .collect(Collectors.toSet());

    assertEquals(
        scanners.size(),
        identifiers.size(),
        "Duplicate ResourceScanner provider/service combinations detected");
  }

  @Test
  void everyScanner_shouldReturnNonNullPermissionsSet() {
    scanners.forEach(
        scanner -> {
          Set<String> permissions = scanner.getPermissionsRequired();

          assertNotNull(
              permissions, scanner.getClass().getSimpleName() + " returned null permissions");

          permissions.forEach(
              permission -> {
                assertNotNull(
                    permission, scanner.getClass().getSimpleName() + " contains a null permission");

                assertFalse(
                    permission.isBlank(),
                    scanner.getClass().getSimpleName() + " contains a blank permission");
              });
        });
  }

  private List<ResourceScanner> discoverScanners() {
    try (ScanResult scanResult =
        new ClassGraph()
            .enableClassInfo()
            .acceptPackages("com.cloudsherpa.ingestion.provider")
            .scan()) {

      return scanResult.getClassesImplementing(ResourceScanner.class.getName()).stream()
          .filter(ClassInfo::isStandardClass)
          .filter(classInfo -> !classInfo.isAbstract())
          .filter(classInfo -> !Modifier.isAbstract(classInfo.loadClass().getModifiers()))
          .map(ClassInfo::loadClass)
          .map(this::instantiateScanner)
          .toList();
    }
  }

  private ResourceScanner instantiateScanner(Class<?> scannerClass) {
    Constructor<?>[] constructors = scannerClass.getDeclaredConstructors();

    if (constructors.length != 1) {
      throw new IllegalStateException(
          "ResourceScanner " + scannerClass.getName() + " must have exactly one constructor");
    }

    Constructor<?> constructor = constructors[0];

    try {
      Object[] dependencies =
          java.util.Arrays.stream(constructor.getParameterTypes()).map(Mockito::mock).toArray();

      constructor.setAccessible(true);

      return (ResourceScanner) constructor.newInstance(dependencies);
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException(
          "Failed to instantiate ResourceScanner: " + scannerClass.getName(), exception);
    }
  }
}
