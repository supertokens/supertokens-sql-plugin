package io.supertokens.storage.sql.test;

import com.tngtech.archunit.base.Optional;
import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.runner.RunWith;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.util.Set;

import static org.apache.tomcat.util.IntrospectionUtils.capitalize;

@RunWith(ArchUnitRunner.class) // Remove this line for JUnit 5!!
@AnalyzeClasses(packages = "io.supertokens.storage.sql.domainobject")
public class EntityAssociationAndAccessPatternTest {
    private static final Set<Class<? extends Annotation>> ASSOCIATIONS = Set.of(ManyToOne.class, OneToMany.class,
            OneToOne.class, ManyToMany.class);

    // @formatter:off
    @ArchTest
    ArchRule allEntityRelationsShouldBeLazyAndHavePrivateGetters = ArchRuleDefinition.classes()
        .that()
        .areAnnotatedWith(Entity.class)
        .or().areAnnotatedWith(Embeddable.class)
        .should(new ArchCondition<JavaClass>(
            "ensure associations have FetchType.LAZY and have a private getter for those associations") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                Set<JavaField> allFields = javaClass.getAllFields();
                for (JavaField field : allFields) {
                    for (JavaAnnotation<JavaField> fieldAnnotation : field.getAnnotations()) {
                        if (isAnAssociationType(fieldAnnotation)) {
                            validateLazyFetching(fieldAnnotation, events, field);
                            validatePrivateGetter(javaClass, events, field);
                        }
                    }
                }
            }

            private boolean isAnAssociationType(JavaAnnotation<JavaField> annotation) {
                return ASSOCIATIONS.stream().anyMatch(clazz -> annotation.getRawType().isEquivalentTo(clazz));
            }

            private void validatePrivateGetter(JavaClass javaClass, ConditionEvents events, JavaField field) {
                Optional<JavaMethod> javaMethodOptional = javaClass.tryGetMethod("get" + capitalize(field.getName()));
                if (javaMethodOptional.isPresent()) {
                    if (!javaMethodOptional.get().getModifiers().contains(JavaModifier.PRIVATE)) {
                        events.add(SimpleConditionEvent.violated(field,
                            "Field " + field.getFullName() + " has public Getter"));
                    }
                }
            }

            private void validateLazyFetching(JavaAnnotation<JavaField> annotation, ConditionEvents events,
                                              JavaField field) {
                JavaEnumConstant fetchType = (JavaEnumConstant) annotation.get("fetch").get();
                if (!FetchType.LAZY.name().equals(fetchType.name())) {
                    String message =
                        field.getDescription() + " is not LAZY in " + field.getSourceCodeLocation();
                    events.add(SimpleConditionEvent.violated(field, message));
                }
            }
        });
    // @formatter:on
}
