package com.example.task.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.beans.factory.annotation.Autowired;

@AnalyzeClasses(
    packages = "com.example.task",
    importOptions = {ImportOption.DoNotIncludeTests.class})
class ArchitectureTest {

  @ArchTest
  static final ArchRule domain_has_no_spring_or_jpa_dependencies =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework..", "jakarta.persistence..", "javax.persistence..");

  @ArchTest
  static final ArchRule application_does_not_depend_on_adapters_or_infrastructure =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..adapter..", "..infrastructure..");

  @ArchTest
  static final ArchRule application_does_not_depend_on_spring_or_jpa =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework..", "jakarta.persistence..", "javax.persistence..");

  @ArchTest
  static final ArchRule no_field_injection_with_autowired =
      noFields().should().beAnnotatedWith(Autowired.class);

  @ArchTest
  static final ArchRule jpa_entities_only_in_persistence_adapter_package =
      classes()
          .that()
          .areAnnotatedWith(Entity.class)
          .should()
          .resideInAPackage("..adapter.out.persistence..");
}
