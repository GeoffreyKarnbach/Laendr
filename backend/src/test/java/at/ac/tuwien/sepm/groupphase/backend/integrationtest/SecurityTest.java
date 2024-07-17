package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.BackendApplication;
import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Security is a cross-cutting concern, however for the sake of simplicity it is tested against the message endpoint
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class SecurityTest implements TestData {

    private static final List<Class<?>> mappingAnnotations = Lists.list(
        RequestMapping.class,
        GetMapping.class,
        PostMapping.class,
        PutMapping.class,
        PatchMapping.class,
        DeleteMapping.class
    );

    private static final List<Class<?>> securityAnnotations = Lists.list(
        Secured.class,
        PreAuthorize.class,
        RolesAllowed.class,
        PermitAll.class,
        DenyAll.class,
        DeclareRoles.class
    );

    @Autowired
    private List<Object> components;

    /**
     * This ensures every Rest Method is secured with Method Security.
     * It is very easy to forget securing one method causing a security vulnerability.
     * Feel free to remove / disable / adapt if you do not use Method Security (e.g. if you prefer Web Security to define who may perform which actions) or want to use Method Security on the service layer.
     */
    @Test
    public void ensureSecurityAnnotationPresentForEveryEndpoint() {
        List<ImmutablePair<Class<?>, Method>> notSecured = components.stream()
            .map(AopUtils::getTargetClass) // beans may be proxies, get the target class instead
            .filter(clazz -> clazz.getCanonicalName() != null && clazz.getCanonicalName().startsWith(BackendApplication.class.getPackageName())) // limit to our package
            .filter(clazz -> clazz.getAnnotation(RestController.class) != null) // limit to classes annotated with @RestController
            .flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods()).map(method -> new ImmutablePair<Class<?>, Method>(clazz, method))) // get all class -> method pairs
            .filter(pair -> Arrays.stream(pair.getRight().getAnnotations()).anyMatch(annotation -> mappingAnnotations.contains(annotation.annotationType()))) // keep only the pairs where the method has a "mapping annotation"
            .filter(pair -> Arrays.stream(pair.getRight().getAnnotations()).noneMatch(annotation -> securityAnnotations.contains(annotation.annotationType()))) // keep only the pairs where the method does not have a "security annotation"
            .toList();

        assertThat(notSecured.size())
            .as("Most rest methods should be secured. If one is really intended for public use, explicitly state that with @PermitAll. "
                + "The following are missing: \n" + notSecured.stream().map(pair -> "Class: " + pair.getLeft() + " Method: " + pair.getRight()).reduce("", (a, b) -> a + "\n" + b))
            .isZero();

    }

    // Testing the security of the specific endpoints happens in their specific files

}
