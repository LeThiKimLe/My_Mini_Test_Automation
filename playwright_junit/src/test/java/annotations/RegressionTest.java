package annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Test
@Tag("regression")
@ExtendWith(AllureDescriptionExtension.class)
@Retention(RUNTIME)
@Target(METHOD)
public @interface RegressionTest {
    String description() default "";
}
