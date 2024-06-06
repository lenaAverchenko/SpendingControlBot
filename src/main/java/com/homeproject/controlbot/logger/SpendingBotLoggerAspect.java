package com.homeproject.controlbot.logger;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Aspect
@Slf4j
public class SpendingBotLoggerAspect {

//    @Around(value = "execution(* com.homeproject.controlbot.service..*.*(..))")
//    public Object adviceAround(ProceedingJoinPoint joinPoint) throws Throwable {
//        String methodName = joinPoint.getSignature().getName();
//        log.info("Method {} from class: {} is going to be started.", methodName, joinPoint.getSignature().getDeclaringTypeName());
//        return tryProceedingMethod(joinPoint);
//    }
    @Around("execution(* com.homeproject.controlbot.service.EarningService.*.*(..)) || " +
            "execution(* com.homeproject.controlbot.service.SpendingService.*.*(..)) || " +
            "execution(* com.homeproject.controlbot.service.SpendingControlBotService.*.*(..)) || " +
            "execution(* com.homeproject.controlbot.helper.*.*(..))"
    )
//            + "execution(* com.homeproject.controlbot.comparator..*.*(..))
    public Object adviceAroundService(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("AOP adviceAround started");
        String methodName = joinPoint.getSignature().getName();
            log.info("AOP Method {} from class: {} is going to be started.", methodName, joinPoint.getSignature().getDeclaringTypeName());
            if (joinPoint.getArgs() != null) {
                List<String> inputValues = Arrays.stream(joinPoint.getArgs()).map(Object::toString).toList();
                log.info("AOP Input parameters are: {}", inputValues);
            }
        log.info("AOP adviceAround ended");
        return tryProceedingMethod(joinPoint);
    }

    public Object tryProceedingMethod(ProceedingJoinPoint joinPoint) throws Throwable{
            Object valueToReturn = null;
            try {
                valueToReturn = joinPoint.proceed();
                if (valueToReturn != null) {
                    log.info("Method {} has ended. The results are: {}",
                            joinPoint.getSignature().getName(), valueToReturn);
                } else {
                    log.info("Method {} has ended.",
                            joinPoint.getSignature().getName());
                }
            } catch (Exception e){
                log.error("Exceprion {} has occurred. Notification is: {}.",
                        e.getClass().getName(), e.getMessage());
                throw e;
            }
            return valueToReturn;
    }
}
