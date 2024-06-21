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
package org.eclipse.tractusx.irs.edc.client.policy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConstraintTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void canReadPermissionsWithDifferentRightOperandAttributeNames() throws JsonProcessingException {

        final List<Permission> permissions = mapper.readerForListOf(Permission.class).readValue("""
                [
                  {
                    "action": "use",
                    "constraint": {
                      "and": [
                        {
                          "leftOperand": "Membership",
                          "operator": {
                            "@id": "eq"
                          },
                          "odrl:rightOperand": "active"
                        },
                        {
                          "leftOperand": "PURPOSE",
                          "operator": {
                            "@id": "eq"
                          },
                          "rightOperand": "ID 3.1 Trace"
                        }
                      ],
                      "or": null
                    }
                  }
                ]
                """);

        assertThat(permissions).isNotEmpty();
        assertThat(permissions.stream()
                              .map(p -> p.getConstraint().getAnd())
                              .flatMap(Collection::stream)
                              .map(Constraint::getRightOperand)).containsExactlyInAnyOrder("active", "ID 3.1 Trace");
    }

}