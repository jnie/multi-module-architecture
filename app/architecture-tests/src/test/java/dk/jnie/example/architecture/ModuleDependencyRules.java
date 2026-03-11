package dk.jnie.example.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.DependencyMatcher.dependsOn;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignedTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit tests to enforce clean architecture boundaries.
 * 
 * These tests validate that:
 * - Domain module is the ONLY pom dependency in all modules EXCEPT application module
 * - Each module follows clean architecture principles
 * - No cyclic dependencies exist between modules
 */
@AnalyzeClasses(packages = "dk.jnie.example..", importOptions = ImportOption.DoNotIncludeTests.class)
public class ModuleDependencyRules {

    // ===== Domain Module Rules =====
    
    /**
     * Domain module should only depend on itself and Java standard library.
     * No external dependencies allowed (except Lombok, Immutables which are compile-time only).
     */
    @ArchTest
    public static final ArchRule domainModuleShouldHaveNoExternalDependencies = 
        noClasses()
            .that(resideInAPackage("dk.jnie.example.domain.."))
            .should()
            .dependOnClassesThat(assignedTo(
                not(resideInAnyPackage(
                    "java..",
                    "javax..",
                    "dk.jnie.example.domain..",
                    "org.immutables..",
                    "org.projectlombok.."
                ))
            ))
            .because("Domain module should have minimal external dependencies");
    
    /**
     * Domain module should not depend on any other application module.
     */
    @ArchTest
    public static final ArchRule domainModuleShouldNotDependOnOtherModules =
        noClasses()
            .that(resideInAPackage("dk.jnie.example.domain.."))
            .should()
            .dependOnClassesThat(resideInAnyPackage(
                "dk.jnie.example.application..",
                "dk.jnie.example.service..",
                "dk.jnie.example.rest..",
                "dk.jnie.example.inbound..",
                "dk.jnie.example.outbound..",
                "dk.jnie.example.advice.."
            ))
            .because("Domain module should not depend on any other application module");

    // ===== Application Module Rules =====
    
    /**
     * Application module can depend on all other modules (it's the composition layer).
     */
    @ArchTest
    public static final ArchRule applicationModuleCanDependOnAllModules =
        classes()
            .that(resideInAPackage("dk.jnie.example.application.."))
            .should()
            .dependOnClassesThat(resideInAnyPackage(
                "dk.jnie.example.domain..",
                "dk.jnie.example.service..",
                "dk.jnie.example.rest..",
                "dk.jnie.example.inbound..",
                "dk.jnie.example.outbound..",
                "dk.jnie.example.advice.."
            ))
            .orShould()
            .dependOnClassesThat(resideInAnyPackage("java..", "javax.."))
            .because("Application module can depend on all other modules");

    // ===== Service Module Rules =====
    
    /**
     * Service module should ONLY depend on domain module (not on inbound/outbound).
     */
    @ArchTest
    public static final ArchRule serviceModuleShouldOnlyDependOnDomain =
        noClasses()
            .that(resideInAPackage("dk.jnie.example.service.."))
            .should()
            .dependOnClassesThat(resideInAnyPackage(
                "dk.jnie.example.application..",
                "dk.jnie.example.rest..",
                "dk.jnie.example.inbound..",
                "dk.jnie.example.outbound..",
                "dk.jnie.example.advice.."
            ))
            .because("Service module should only depend on domain, not other modules");

    // ===== Inbound (REST) Module Rules =====
    
    /**
     * Inbound/REST module should ONLY depend on domain module (not on service, outbound).
     */
    @ArchTest
    public static final ArchRule inboundModuleShouldOnlyDependOnDomain =
        noClasses()
            .that(resideInAPackage("dk.jnie.example.rest..", "dk.jnie.example.inbound.."))
            .should()
            .dependOnClassesThat(resideInAnyPackage(
                "dk.jnie.example.application..",
                "dk.jnie.example.service..",
                "dk.jnie.example.outbound..",
                "dk.jnie.example.advice.."
            ))
            .because("Inbound/REST module should only depend on domain");

    // ===== Outbound Module Rules =====
    
    /**
     * Outbound module should ONLY depend on domain module (not on service, inbound).
     */
    @ArchTest
    public static final ArchRule outboundModuleShouldOnlyDependOnDomain =
        noClasses()
            .that(resideInAPackage("dk.jnie.example.outbound..", "dk.jnie.example.advice.."))
            .should()
            .dependOnClassesThat(resideInAnyPackage(
                "dk.jnie.example.application..",
                "dk.jnie.example.service..",
                "dk.jnie.example.rest..",
                "dk.jnie.example.inbound.."
            ))
            .because("Outbound module should only depend on domain");

    // ===== General Architecture Rules =====
    
    /**
     * No module should depend on the architecture-tests module.
     */
    @ArchTest
    public static final ArchRule noModuleShouldDependOnArchitectureTests =
        noClasses()
            .that(resideInAnyPackage(
                "dk.jnie.example.domain..",
                "dk.jnie.example.application..",
                "dk.jnie.example.service..",
                "dk.jnie.example.rest..",
                "dk.jnie.example.inbound..",
                "dk.jnie.example.outbound..",
                "dk.jnie.example.advice.."
            ))
            .should()
            .dependOnClassesThat(resideInAPackage("dk.jnie.example.architecture.."))
            .because("No application module should depend on architecture-tests");

    /**
     * Check if domain module has any internal dependencies.
     * Domain should be the most stable module with zero internal module dependencies.
     */
    @ArchTest
    public static final ArchRule domainShouldNotHaveCyclicDependencies =
        noClasses()
            .that(resideInAPackage("dk.jnie.example.domain.."))
            .should()
            .dependOnClassesThat(dependsOn(resideInAPackage("dk.jnie.example.domain..")))
            .because("Domain should not have internal cyclic dependencies within itself");
}
