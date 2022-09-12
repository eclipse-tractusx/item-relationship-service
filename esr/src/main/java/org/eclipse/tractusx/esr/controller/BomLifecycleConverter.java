/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.esr.controller;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Custom converter from String to {@link BomLifecycle}.
 * Used to convert path parameter.
 */
@Component
class BomLifecycleConverter implements Converter<String, BomLifecycle> {

    /**
     * Converts from String to BomLifecycle
     * @param value string
     * @return
     */
    @Override
    public BomLifecycle convert(final String value) {
        return Stream.of(BomLifecycle.values())
                     .filter(bomLifecycle -> bomLifecycle.getName().equals(value))
                     .findFirst()
                     .orElseThrow(() -> new NoSuchElementException("Unsupported BomLifecycle: " + value
                             + ". Must be one of: " + supportedBomLifecycles()));
    }

    private static String supportedBomLifecycles() {
        return Stream.of(BomLifecycle.values()).map(BomLifecycle::getName).collect(Collectors.joining(", "));
    }

}
