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
package org.eclipse.tractusx.irs;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

/**
 * The {@code SemanticModelNames} class contains constants representing all available semantic model names.
 * <p>
 * These constants can be used throughout the application to refer to specific semantic models in a consistent manner.
 * </p>
 */
@NoArgsConstructor(access = PRIVATE)
public final class SemanticModelNames {
    public static final String SINGLE_LEVEL_BOM_AS_BUILT_2_0_0 = "urn:samm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt";
    public static final String SINGLE_LEVEL_BOM_AS_BUILT_3_0_0 = "urn:samm:io.catenax.single_level_bom_as_built:3.0.0#SingleLevelBomAsBuilt";
    public static final String SINGLE_LEVEL_BOM_AS_BUILT_3_1_0 = "urn:samm:io.catenax.single_level_bom_as_built:3.1.0#SingleLevelBomAsBuilt";

    public static final String SINGLE_LEVEL_BOM_AS_PLANNED_2_0_0 = "urn:samm:io.catenax.single_level_bom_as_planned:2.0.0#SingleLevelBomAsPlanned";
    public static final String SINGLE_LEVEL_BOM_AS_PLANNED_3_0_0 = "urn:samm:io.catenax.single_level_bom_as_planned:3.0.0#SingleLevelBomAsPlanned";
    public static final String SINGLE_LEVEL_BOM_AS_PLANNED_3_1_0 = "urn:samm:io.catenax.single_level_bom_as_planned:3.1.0#SingleLevelBomAsPlanned";

    public static final String SINGLE_LEVEL_BOM_AS_SPECIFIED_1_0_0 = "urn:samm:io.catenax.single_level_bom_as_specified:1.0.0#SingleLevelBomAsSpecified";
    public static final String SINGLE_LEVEL_BOM_AS_SPECIFIED_1_1_0 = "urn:samm:io.catenax.single_level_bom_as_specified:1.1.0#SingleLevelBomAsSpecified";
    public static final String SINGLE_LEVEL_BOM_AS_SPECIFIED_2_0_0 = "urn:samm:io.catenax.single_level_bom_as_specified:2.0.0#SingleLevelBomAsSpecified";
    public static final String SINGLE_LEVEL_BOM_AS_SPECIFIED_2_1_0 = "urn:samm:io.catenax.single_level_bom_as_specified:2.1.0#SingleLevelBomAsSpecified";

    public static final String SINGLE_LEVEL_USAGE_AS_BUILT_2_0_0 = "urn:samm:io.catenax.single_level_usage_as_built:2.0.0#SingleLevelUsageAsBuilt";
    public static final String SINGLE_LEVEL_USAGE_AS_BUILT_3_0_0 = "urn:samm:io.catenax.single_level_usage_as_built:3.0.0#SingleLevelUsageAsBuilt";
    public static final String SINGLE_LEVEL_USAGE_AS_BUILT_3_1_0 = "urn:samm:io.catenax.single_level_usage_as_built:3.1.0#SingleLevelUsageAsBuilt";

    public static final String SINGLE_LEVEL_USAGE_AS_PLANNED_2_0_0 = "urn:samm:io.catenax.single_level_usage_as_planned:2.0.0#SingleLevelUsageAsPlanned";
    public static final String SINGLE_LEVEL_USAGE_AS_PLANNED_2_1_0 = "urn:samm:io.catenax.single_level_usage_as_planned:2.1.0#SingleLevelUsageAsPlanned";

    public static final String SERIAL_PART_3_0_0 = "urn:samm:io.catenax.serial_part:3.0.0#SerialPart";
    public static final String PART_AS_PLANNED_1_0_1 = "urn:samm:io.catenax.part_as_planned:1.0.1#PartAsPlanned";
    public static final String PART_AS_PLANNED_2_0_0 = "urn:samm:io.catenax.part_as_planned:2.0.0#PartAsPlanned";
    public static final String PART_AS_SPECIFIED_3_0_0 = "urn:samm:io.catenax.part_as_specified:3.0.0#PartAsSpecified";
    public static final String BATCH_3_0_0 = "urn:samm:io.catenax.batch:3.0.0#Batch";
    public static final String MATERIAL_FOR_RECYCLING_1_1_0 = "urn:samm:io.catenax.material_for_recycling:1.1.0#MaterialForRecycling";
    public static final String BATTERY_PRODUCT_DESCRIPTION_1_0_1 = "urn:samm:io.catenax.battery.product_description:1.0.1#ProductDescription";
    public static final String PHYSICAL_DIMENSION_1_0_0 = "urn:samm:io.catenax.physical_dimension:1.0.0#PhysicalDimension";
    public static final String PART_AS_SPECIFIED_2_0_0 = "urn:samm:io.catenax.part_as_specified:2.0.0#PartAsSpecified";
    public static final String PART_SITE_INFORMATION_AS_PLANNED_1_0_0 = "urn:samm:io.catenax.part_site_information_as_planned:1.0.0#PartSiteInformationAsPlanned";

    public static final String VEHICLE_PRODUCT_DESCRIPTION_1_0_0 = "urn:samm:io.catenax.vehicle.product_description:1.0.0#ProductDescription";
    public static final String SECONDARY_MATERIAL_CONTENT_VERIFIABLE_1_0_0 = "urn:samm:io.catenax.secondary_material_content_verifiable:1.0.0#SecondaryMaterialContentVerifiable";
    public static final String JUST_IN_SEQUENCE_PART_3_0_0 = "urn:samm:io.catenax.just_in_sequence_part:3.0.0#JustInSequencePart";
    public static final String TRANSMISSION_PASS_1_0_0 = "urn:samm:io.catenax.transmission_pass:1.0.0#TransmissionPass";
    public static final String MARKET_PLACE_OFFER_1_4_0 = "urn:samm:io.catenax.market_place_offer:1.4.0#MarketPlaceOffer";
    public static final String DIGITAL_PRODUCT_PASSPORT_1_0_0 = "urn:samm:io.catenax.digital_product_passport:1.0.0#DigitalProductPassport";
    public static final String BATTERY_BATTERY_PASS_3_0_1 = "urn:samm:io.catenax.battery.battery_pass:3.0.1#BatteryPass";
    public static final String TRACTION_BATTERY_CODE_1_0_0 = "urn:samm:io.catenax.traction_battery_code:1.0.0#TractionBatteryCode";
    public static final String RETURN_REQUEST_1_0_1 = "urn:samm:io.catenax.return_request:1.0.1#ReturnRequest";
    public static final String SECONDARY_MATERIAL_CONTENT_CALCULATED_1_0_0 = "urn:samm:io.catenax.secondary_material_content_calculated:1.0.0#SecondaryMaterialContentCalculated";

}
