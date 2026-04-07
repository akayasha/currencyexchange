package com.example.currencyexchange.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * AspectJ-based logging aspect that intercepts all controller and service methods.
 *
 * Logs:
 *  - Method entry with arguments (request)
 *  - Method exit with return value (response)
 *  - Execution time
 *  - Exceptions
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Pointcut targeting all methods in the controller package.
     */
    @Pointcut("execution(* com.example.currencyexchange.controller..*(..))")
    public void controllerMethods() {}

    /**
     * Pointcut targeting all methods in the service package.
     */
    @Pointcut("execution(* com.example.currencyexchange.service..*(..))")
    public void serviceMethods() {}

    /**
     * Around advice for controllers: logs request, response, and duration.
     */
    @Around("controllerMethods()")
    public Object logControllerCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("[REQUEST ] {}.{}() | args: {}", className, methodName, Arrays.toString(args));

        long start = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Exception ex) {
            log.error("[EXCEPTION] {}.{}() | error: {}", className, methodName, ex.getMessage());
            throw ex;
        }
        long duration = System.currentTimeMillis() - start;

        log.info("[RESPONSE] {}.{}() | duration: {}ms | result: {}", className, methodName, duration, result);
        return result;
    }

    /**
     * Before advice for service methods: logs method entry.
     */
    @Before("serviceMethods()")
    public void logServiceEntry(JoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.debug("[SERVICE ENTER] {}.{}() | args: {}", className, methodName, Arrays.toString(args));
    }

    /**
     * AfterReturning advice for service methods: logs return value.
     */
    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logServiceExit(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        log.debug("[SERVICE EXIT ] {}.{}() | returned: {}", className, methodName, result);
    }

    /**
     * AfterThrowing advice for service methods: logs exceptions thrown.
     */
    @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
    public void logServiceException(JoinPoint joinPoint, Throwable ex) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        log.error("[SERVICE ERROR] {}.{}() | exception: {} - {}", className, methodName,
                ex.getClass().getSimpleName(), ex.getMessage());
    }
}
