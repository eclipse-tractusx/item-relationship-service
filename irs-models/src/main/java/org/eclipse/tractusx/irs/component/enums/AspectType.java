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
    SERIAL_PART_TYPIZATION(AspectTypesConstants.SERIAL_PART_TYPIZATION),
    ASSEMBLY_PART_RELATIONSHIP(AspectTypesConstants.ASSEMBLY_PART_RELATIONSHIP),
    BATCH(AspectTypesConstants.BATCH),
    PRODUCT_DESCRIPTION(AspectTypesConstants.PRODUCT_DESCRIPTION),
    ID_CONVERSION(AspectTypesConstants.ID_CONVERSION),
    MARKETPLACE_OFFER(AspectTypesConstants.MARKETPLACE_OFFER),
    MATERIAL_FOR_RECYCLING(AspectTypesConstants.MATERIAL_FOR_RECYCLING),
    PHYSICAL_DIMENSION(AspectTypesConstants.PHYSICAL_DIMENSION),
    RETURN_REQUEST(AspectTypesConstants.RETURN_REQUEST),
    CERTIFICATE_OF_DESTRUCTION(AspectTypesConstants.CERTIFICATE_OF_DESTRUCTION),
    CERTIFICATE_OF_DISMANTLER(AspectTypesConstants.CERTIFICATE_OF_DISMANTLER),
    END_OF_LIFE(AspectTypesConstants.END_OF_LIFE),
    PCF_CORE(AspectTypesConstants.PCF_CORE),
    PCF_SUPPLY_RELATION(AspectTypesConstants.PCF_SUPPLY_RELATION),
    PCF_TECHNICAL(AspectTypesConstants.PCF_TECHNICAL),
    ADDRESS_ASPECT(AspectTypesConstants.ADDRESS_ASPECT),
    CONTACT_INFORMATION(AspectTypesConstants.CONTACT_INFORMATION),
    BATTERY_PASS(AspectTypesConstants.BATTERY_PASS),
    VEHICLE_DIAGNOSTIC_DATA_QUALITY(AspectTypesConstants.VEHICLE_DIAGNOSTIC_DATA_QUALITY),
    SINGLE_LEVEL_BOM_AS_PLANNED(AspectTypesConstants.SINGLE_LEVEL_BOM_AS_PLANNED),
    PART_AS_PLANNED(AspectTypesConstants.PART_AS_PLANNED);

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
        public static final String SERIAL_PART_TYPIZATION = "SerialPartTypization";
        public static final String ASSEMBLY_PART_RELATIONSHIP = "AssemblyPartRelationship";
        public static final String BATCH = "Batch";
        public static final String PRODUCT_DESCRIPTION = "ProductDescription";
        public static final String ID_CONVERSION = "IdConversion";
        public static final String MARKETPLACE_OFFER = "MarketplaceOffer";
        public static final String MATERIAL_FOR_RECYCLING = "MaterialForRecycling";
        public static final String PHYSICAL_DIMENSION = "PhysicalDimension";
        public static final String RETURN_REQUEST = "ReturnRequest";
        public static final String CERTIFICATE_OF_DESTRUCTION = "CertificateOfDestruction";
        public static final String CERTIFICATE_OF_DISMANTLER = "CertificateOfDismantler";
        public static final String END_OF_LIFE = "EndOfLife";
        public static final String PCF_CORE = "PcfCore";
        public static final String PCF_SUPPLY_RELATION = "PcfSupplyRelation";
        public static final String PCF_TECHNICAL = "PcfTechnical";
        public static final String ADDRESS_ASPECT = "AddressAspect";
        public static final String CONTACT_INFORMATION = "ContactInformation";
        public static final String BATTERY_PASS = "BatteryPass";
        public static final String VEHICLE_DIAGNOSTIC_DATA_QUALITY = "VehicleDiagnosticDataQuality";
        public static final String SINGLE_LEVEL_BOM_AS_PLANNED = "SingleLevelBomAsPlanned";
        public static final String PART_AS_PLANNED = "PartAsPlanned";
    }
}
