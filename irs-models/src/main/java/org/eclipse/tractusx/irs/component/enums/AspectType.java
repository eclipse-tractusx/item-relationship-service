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
package org.eclipse.tractusx.irs.component.enums;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * AspectType information for a part tree
 */
@JsonSerialize(using = ToStringSerializer.class)
@SuppressWarnings("PMD.ShortMethodName")
public enum AspectType {
    ADDRESS_ASPECT(AspectTypesConstants.ADDRESS_ASPECT),
    BATCH(AspectTypesConstants.BATCH),
    BATTERY_PASS(AspectTypesConstants.BATTERY_PASS),
    CERTIFICATE_OF_DESTRUCTION(AspectTypesConstants.CERTIFICATE_OF_DESTRUCTION),
    CERTIFICATE_OF_DISMANTLER(AspectTypesConstants.CERTIFICATE_OF_DISMANTLER),
    CHARGING_PROCESS(AspectTypesConstants.CHARGING_PROCESS),
    CLAIM_DATA(AspectTypesConstants.CLAIM_DATA),
    DIAGNOSTIC_DATA(AspectTypesConstants.DIAGNOSTIC_DATA),
    END_OF_LIFE(AspectTypesConstants.END_OF_LIFE),
    ESR_CERTIFICATE(AspectTypesConstants.ESR_CERTIFICATE),
    ESR_CERTIFICATE_STATISTICS(AspectTypesConstants.ESR_CERTIFICATE_STATISTICS),
    ID_CONVERSION(AspectTypesConstants.ID_CONVERSION),
    MARKETPLACE_OFFER(AspectTypesConstants.MARKETPLACE_OFFER),
    MATERIAL_FOR_HOMOLOGATION(AspectTypesConstants.MATERIAL_FOR_HOMOLOGATION),
    MATERIAL_FOR_RECYCLING(AspectTypesConstants.MATERIAL_FOR_RECYCLING),
    PART_AS_PLANNED(AspectTypesConstants.PART_AS_PLANNED),
    PART_AS_SPECIFIED(AspectTypesConstants.PART_AS_SPECIFIED),
    PHYSICAL_DIMENSION(AspectTypesConstants.PHYSICAL_DIMENSION),
    PRODUCT_DESCRIPTION(AspectTypesConstants.PRODUCT_DESCRIPTION),
    RETURN_REQUEST(AspectTypesConstants.RETURN_REQUEST),
    SERIAL_PART(AspectTypesConstants.SERIAL_PART),
    PART_SITE_INFORMATION_AS_PLANNED(AspectTypesConstants.PART_SITE_INFORMATION_AS_PLANNED),
    SINGLE_LEVEL_BOM_AS_BUILT(AspectTypesConstants.SINGLE_LEVEL_BOM_AS_BUILT),
    SINGLE_LEVEL_BOM_AS_PLANNED(AspectTypesConstants.SINGLE_LEVEL_BOM_AS_PLANNED),
    SINGLE_LEVEL_BOM_AS_SPECIFIED(AspectTypesConstants.SINGLE_LEVEL_BOM_AS_SPECIFIED),
    SINGLE_LEVEL_USAGE_AS_BUILT(AspectTypesConstants.SINGLE_LEVEL_USAGE_AS_BUILT);

    private final String name;

    AspectType(final String name) {
        this.name = name;
    }

    /**
     * of as a substitute/alias for valueOf handling the default value
     *
     * @param value see {@link #name}
     * @return the corresponding AspectType
     */
    public static AspectType value(final String value) {
        return AspectType.valueOf(value);
    }

    @JsonCreator
    public static AspectType fromValue(final String value) {
        return Stream.of(AspectType.values())
                     .filter(aspectType -> aspectType.name.equals(value))
                     .findFirst()
                     .orElseThrow(() -> new NoSuchElementException("Unsupported AspectType: " + value
                             + ". Must be one of: " + supportedAspectTypes()));
    }

    private static String supportedAspectTypes() {
        return Stream.of(AspectType.values()).map(aspect -> aspect.name).collect(Collectors.joining(", "));
    }

    /**
     * @return convert AspectType to string value
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Constants for aspectTypes
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class AspectTypesConstants {
        public static final String ADDRESS_ASPECT = "AddressAspect";
        public static final String BATCH = "Batch";
        public static final String BATTERY_PASS = "BatteryPass";
        public static final String CERTIFICATE_OF_DESTRUCTION = "CertificateOfDestruction";
        public static final String CERTIFICATE_OF_DISMANTLER = "CertificateOfDismantler";
        public static final String CHARGING_PROCESS = "ChargingProcess";
        public static final String CLAIM_DATA = "ClaimData";
        public static final String DIAGNOSTIC_DATA = "DiagnosticData";
        public static final String END_OF_LIFE = "EndOfLife";
        public static final String ESR_CERTIFICATE = "EsrCertificate";
        public static final String ESR_CERTIFICATE_STATISTICS = "EsrCertificateStateStatistic";
        public static final String ID_CONVERSION = "IdConversion";
        public static final String MARKETPLACE_OFFER = "MarketplaceOffer";
        public static final String MATERIAL_FOR_HOMOLOGATION = "MaterialForHomologation";
        public static final String MATERIAL_FOR_RECYCLING = "MaterialForRecycling";
        public static final String PART_AS_PLANNED = "PartAsPlanned";
        public static final String PART_AS_SPECIFIED = "PartAsSpecified";
        public static final String PHYSICAL_DIMENSION = "PhysicalDimension";
        public static final String PRODUCT_DESCRIPTION = "ProductDescription";
        public static final String RETURN_REQUEST = "ReturnRequest";
        public static final String SERIAL_PART = "SerialPart";
        public static final String PART_SITE_INFORMATION_AS_PLANNED = "PartSiteInformationAsPlanned";
        public static final String SINGLE_LEVEL_BOM_AS_BUILT = "SingleLevelBomAsBuilt";
        public static final String SINGLE_LEVEL_BOM_AS_PLANNED = "SingleLevelBomAsPlanned";
        public static final String SINGLE_LEVEL_BOM_AS_SPECIFIED = "SingleLevelBomAsSpecified";
        public static final String SINGLE_LEVEL_USAGE_AS_BUILT = "SingleLevelUsageAsBuilt";
    }
}
