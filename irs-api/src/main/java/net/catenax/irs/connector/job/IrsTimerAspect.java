//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.connector.job;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.services.MeterRegistryService;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Define the pointcut and joinpoint for IRS Timer
 */
@Aspect
@Configuration
@EnableAspectJAutoProxy
@Slf4j
public class IrsTimerAspect {
    private static final String DEFAULT_TAG = "job_process";

    @Autowired
    private MeterRegistryService meterRegistryService;

    @Around("@annotation(net.catenax.irs.connector.job.IrsTimer)")
    public Object measureJobExecutionTimer(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        final IrsTimer timer = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod()
                                                                                     .getAnnotation(IrsTimer.class);
        final long startTime = System.currentTimeMillis();
        final Object object = proceedingJoinPoint.proceed();
        final long duration = System.currentTimeMillis() - startTime;

        final String tag = StringUtils.isBlank(timer.value()) ? DEFAULT_TAG : timer.value();
        meterRegistryService.setMeasuredMethodExecutionTime(tag, duration);

        return object;
    }

}
