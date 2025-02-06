/********************************************************************************
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
package org.eclipse.tractusx.irs.testing.testdata;

import java.time.ZonedDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SerialPart {
    List<LocalIdentifier> localIdentifiers;
    ManufacturingInformation manufacturingInformation;
    String catenaXId;
    PartTypeInformation partTypeInformation;

    @Value
    @Builder
    @Jacksonized
    public static class LocalIdentifier {
        String value;
        String key;
    }

    @Value
    @Builder
    @Jacksonized
    public static class ManufacturingInformation {
        ZonedDateTime date;
        String country;
        List<Site> sites;
    }

    @Value
    @Builder
    @Jacksonized
    public static class Site {
        String catenaXsiteId;
        String function;
    }

    @Value
    @Builder
    @Jacksonized
    public static class PartTypeInformation {
        String manufacturerPartId;
        String customerPartId;
        List<PartClassification> partClassification;
        String nameAtManufacturer;
        String nameAtCustomer;
    }

    @Value
    @Builder
    @Jacksonized
    public static class PartClassification {
        String classificationDescription;
        String classificationStandard;
        String classificationID;
    }
}
