package annotations;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;

public class AllureDescriptionExtension implements BeforeTestExecutionCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        Optional<Method> testMethod = context.getTestMethod();
        if (!testMethod.isPresent()) {
            return;
        }
        String description = getDescription(testMethod.get());
        String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (!description.isEmpty()) {
            Allure.getLifecycle().updateTestCase(result -> result.setDescription(String.format("[Test started at: %s] \n %s", startTime, description)));
        }
    }

    private String getDescription(Method method) {
        // First check for Allure @Description annotation
        Description descriptionAnnotation = method.getAnnotation(Description.class);
        if (descriptionAnnotation != null && !descriptionAnnotation.value().isEmpty()) {
            return descriptionAnnotation.value();
        }

        // Then check for JUnit 5 @DisplayName annotation
        DisplayName displayNameAnnotation = method.getAnnotation(DisplayName.class);
        if (displayNameAnnotation != null && !displayNameAnnotation.value().isEmpty()) {
            return displayNameAnnotation.value();
        }

        // Fall back to custom annotations
        SmokeTest smokeTest = method.getAnnotation(SmokeTest.class);
        if (smokeTest != null) {
            return smokeTest.description();
        }

        RegressionTest regressionTest = method.getAnnotation(RegressionTest.class);
        if (regressionTest != null) {
            return regressionTest.description();
        }

        SmokeRegressionTest smokeRegressionTest = method.getAnnotation(SmokeRegressionTest.class);
        if (smokeRegressionTest != null) {
            return smokeRegressionTest.description();
        }

        return "";
    }
}
