package annotations;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.qameta.allure.Allure;

public class AllureDescriptionExtension implements BeforeTestExecutionCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        Optional<Method> testMethod = context.getTestMethod();
        if (!testMethod.isPresent()) {
            return;
        }

        String description = getDescription(testMethod.get());
        if (!description.isEmpty()) {
            Allure.getLifecycle().updateTestCase(result -> result.setDescription(description));
        }
    }

    private String getDescription(Method method) {
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
