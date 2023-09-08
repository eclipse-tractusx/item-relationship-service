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
package org.eclipse.tractusx.irs.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.data.StringMapper.mapFromString;

import java.util.Map;
import java.util.Set;

import org.eclipse.tractusx.irs.aaswrapper.job.IntegrityAspect;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.component.Submodel;
import org.eclipse.tractusx.irs.component.enums.IntegrityState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataIntegrityServiceTest {

    private DataIntegrityService dataIntegrityService;

    private String publicKey = "-----BEGIN PUBLIC KEY-----\n"
            + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6h9Uz0RH9xwlb/rqSws2\n"
            + "JccIs817v1wfgLDGDY36YwzKVBNm+5IUJvCwLeMRZJ6otUFYQUUInQgM6MgW90z7\n"
            + "k9SjUB7UxiSqZFfwIF6uanN0HisEo5dCfGSzLQBIrg1nw/CPKGf8Vn5kZ/+5aBl3\n"
            + "dLu8pht+29yALFNZxThmnUx2rmTB9fH0SKtgY9HxBzQlbYuiwhqgbpnWTgCQb+F5\n"
            + "6Q3TnvXyG3DkOMYvzdle/QLPm+ywSf1FKp/qOaX483yVN5GpkWkQJwA5fsBGFkmT\n"
            + "7hmisXEyTiIps5yR/scAOjj7evQk6+FqRksPr8mOwYwBU7F6LjnJRtg9WeHZn0CA\n"
            + "GQIDAQAB\n"
            + "-----END PUBLIC KEY-----";

    @BeforeEach
    void setUp() {
        dataIntegrityService = new DataIntegrityService(publicKey);
    }

    @Test
    void shouldValidateDataChainIntegrityWhenHashAndSignatureMatches() {
        // Arrange
        final IntegrityAspect.Reference reference = new IntegrityAspect.Reference("testType",
                "3d9a14153459ef617d86116297fb63d37948bffc9247bb969b63af29fe9aac6f", "a330b06d73ec0747fd7da4c099f5687eba57766f8a33ae02ba23c2f239a82a6c14b9701c1d57f946de4363453a4974231a02180fc2f44be313b2da3a83cd56ef4077ac75ce2bab32d7a6402dfa7ab98a4e54320b66b9d087d07ee2f516fab929d270485d666b96872ae251881e3698506905b28842b070e1e8ffb8509f0a183396754fdadc75028b72b3e23aa7525d4b47cf4af947b9c2715ea46cb61cc5a834e35496b03a624da92b65ebe2124d76e5cf0e5520fc0841d33b80072a05e595c5c29d9db6b5e94ec3c83efd11b80ff639e168dd578b6b92d4e8143dfdcedf43495654f5ccd388f0233c082652a1c1eb8bfccae6d065b481f653674773a4cbbb07");
        final IntegrityAspect.ChildData childData = new IntegrityAspect.ChildData("child-id", Set.of(reference));
        final IntegrityAspect integrityAspect = new IntegrityAspect("parent-id", Set.of(childData));
        final ItemContainer build = ItemContainer.builder().submodel(getSubmodel()).integrity(integrityAspect).build();

        // Act
        final IntegrityState integrityState = dataIntegrityService.chainDataIntegrityIsValid(build);

        // Assert
        assertThat(integrityState).isEqualTo(IntegrityState.INVALID);
    }

    @Test
    void shouldFailDataChainValidationWhenPresentedHashIsWrong() {
        // Arrange
        final IntegrityAspect.Reference reference = new IntegrityAspect.Reference("testType", "wrongHash", null);
        final IntegrityAspect.ChildData childData = new IntegrityAspect.ChildData("child-id", Set.of(reference));
        final IntegrityAspect integrityAspect = new IntegrityAspect("parent-id", Set.of(childData));
        final ItemContainer build = ItemContainer.builder().submodel(getSubmodel()).integrity(integrityAspect).build();

        // Act
        final IntegrityState integrityState = dataIntegrityService.chainDataIntegrityIsValid(build);

        // Assert
        assertThat(integrityState).isEqualTo(IntegrityState.INVALID);
    }

    @Test
    void shouldFailDataChainValidationWhenPresentedSignatureIsWrong() {
        // Arrange
        final IntegrityAspect.Reference reference = new IntegrityAspect.Reference("testType", "3d9a14153459ef617d86116297fb63d37948bffc9247bb969b63af29fe9aac6f", "30b06d73ec07");
        final IntegrityAspect.ChildData childData = new IntegrityAspect.ChildData("child-id", Set.of(reference));
        final IntegrityAspect integrityAspect = new IntegrityAspect("parent-id", Set.of(childData));
        final ItemContainer build = ItemContainer.builder().submodel(getSubmodel()).integrity(integrityAspect).build();

        // Act
        final IntegrityState integrityState = dataIntegrityService.chainDataIntegrityIsValid(build);

        // Assert
        assertThat(integrityState).isEqualTo(IntegrityState.INVALID);
    }

    private Submodel getSubmodel() {
        final String payload = "{\"test\":\"test\"}";

        return Submodel.builder()
                       .aspectType("testType")
                       .catenaXId("child-id")
                       .identification("child-identifier")
                       .payload(mapFromString(payload, Map.class))
                       .build();
    }
}