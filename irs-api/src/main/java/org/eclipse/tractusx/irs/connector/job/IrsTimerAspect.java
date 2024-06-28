/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.connector.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.eclipse.tractusx.irs.services.MeterRegistryService;
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

    @Around("@annotation(org.eclipse.tractusx.irs.connector.job.IrsTimer)")
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
