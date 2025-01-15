/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.services;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory class for constructing ExecutorCompletionService instances.
 */
@Component
@SuppressWarnings({ "PMD.MissingStaticMethodInNonInstantiatableClass" })
public final class ExecutorCompletionServiceFactory {

    private final int threadCount;

    private ExecutorCompletionServiceFactory(@Value("${irs.job.batch.threadCount}") final int threadCount) {
        this.threadCount = threadCount;
    }

    public <T> ExecutorCompletionService<T> create() {
        return new ExecutorCompletionService<>(Executors.newFixedThreadPool(threadCount));
    }
}
