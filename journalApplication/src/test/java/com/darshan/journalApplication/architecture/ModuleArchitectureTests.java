package com.darshan.journalApplication.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ModuleArchitectureTests {
    private final JavaClasses applicationClasses = new ClassFileImporter()
            .importPackages("com.darshan.journalApplication");

    @Test
    void journalAndTagModulesDoNotDependOnIdentityPersistence() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("..journal..", "..tag..")
                .should().dependOnClassesThat().haveFullyQualifiedName(
                        "com.darshan.journalApplication.entity.User")
                .orShould().dependOnClassesThat().haveFullyQualifiedName(
                        "com.darshan.journalApplication.repository.UserEntryRepository")
                .orShould().dependOnClassesThat().haveFullyQualifiedName(
                        "com.darshan.journalApplication.service.UserEntryService");

        rule.check(applicationClasses);
    }
}
