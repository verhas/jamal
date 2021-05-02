package javax0.jamal.test.tools.junit;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtendWith(IntelliJOnly.IntelliJOnlyCondition.class)
public @interface IntelliJOnly {

    class IntelliJOnlyCondition implements ExecutionCondition {
        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            final Method method = context.getRequiredTestMethod();
            final var annotation = method.getDeclaredAnnotation(IntelliJOnly.class);
            if (annotation == null) {
                throw new ExtensionConfigurationException("Could not find @" + IntelliJOnly.class + " annotation on the method " + method);
            }
            boolean isIntelliJStarted = false;
            final var st = new Exception().getStackTrace();
            for (final var s : st) {
                if (s.getClassName().contains("Idea")) {
                    isIntelliJStarted = true;
                    break;
                }
            }
            return isIntelliJStarted ? ConditionEvaluationResult.enabled("started from IntelliJ") : ConditionEvaluationResult.disabled("not started from IntelliJ");
        }
    }
}
