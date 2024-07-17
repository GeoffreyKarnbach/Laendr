package at.ac.tuwien.sepm.groupphase.backend.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@ConditionalOnProperty(name = "logging.aspect.enabled", havingValue = "true")
public class LoggingAspect {

    @Value("${logging.aspect.arg-string-length}")
    private int argStringLength;

    @Pointcut("within(at.ac.tuwien.sepm.groupphase.backend.service..*) || within(@org.springframework.stereotype.Repository *)")
    public void beanPointcut() {
    }

    @Around("beanPointcut()")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        log.debug("Before method: " + joinPoint.getSignature().getDeclaringType().getSimpleName() + "."
            + joinPoint.getSignature().getName() + " called with argument(s): " + getJoinPointArgs(joinPoint));

        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log.debug("After method in " + duration + "ms: " + joinPoint.getSignature().getDeclaringType().getSimpleName() + "."
            + joinPoint.getSignature().getName() + " with result: " + limitString(result != null ? result.toString() : "null"));
        return result;
    }

    private String getJoinPointArgs(ProceedingJoinPoint joinPoint) {
        var params = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        var args = joinPoint.getArgs();

        var sb = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(params != null ? params[i] : "");
            sb.append("=");
            sb.append(limitString(args[i] != null ? args[i].toString() : "null"));
        }

        return sb.toString();
    }

    private String limitString(String input) {
        if (input.length() <= argStringLength) {
            return input;
        } else {
            return input.substring(0, argStringLength) + "...";
        }
    }

}
