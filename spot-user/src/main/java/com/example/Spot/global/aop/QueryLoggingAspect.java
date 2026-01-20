package com.example.Spot.global.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class QueryLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(QueryLoggingAspect.class);

    @Pointcut("execution(* com.example.Spot.global.feign.*Client.*(..))")
    public void feignClientMethods() {
    }

    @Around("feignClientMethods()")
    public Object logFeignCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.info("[Feign Call Start] {}.{}", className, methodName);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            log.info("[Feign Call End] {}.{} - {}ms", className, methodName, (endTime - startTime));
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("[Feign Call Error] {}.{} - {}ms - Error: {}",
                    className, methodName, (endTime - startTime), e.getMessage());
            throw e;
        }
    }
}
