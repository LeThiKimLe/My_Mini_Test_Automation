package annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Test
@Tag("smoke")
@ExtendWith(AllureDescriptionExtension.class)
@Retention(RUNTIME)
@Target(METHOD)
public @interface SmokeTest {
    String description() default "";
}
