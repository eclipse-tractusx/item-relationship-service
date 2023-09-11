/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.policystore.models;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;

/**
 * A LogicalConstraintType object use in Permission
 */
@JsonSerialize(using = ToStringSerializer.class)
@Getter
public enum LogicalConstraintType {

    AND("and"),
    OR("or"),
    XONE("xone"),
    ANDSEQUENCE("andsequence");

    private String code;

    LogicalConstraintType(String code) {
        this.code = code;
    }

    @JsonCreator
    public static LogicalConstraintType fromValue(final String value) {
        return Stream.of(LogicalConstraintType.values())
                     .filter(logicalConstraintType -> logicalConstraintType.code.equals(value))
                     .findFirst()
                     .orElseThrow(() -> new NoSuchElementException("Unsupported LogicalConstraintType: " + value));
    }

    /**
     * @return convert OperatorType to string value
     */
    @Override
    public String toString() {
        return code;
    }

}
