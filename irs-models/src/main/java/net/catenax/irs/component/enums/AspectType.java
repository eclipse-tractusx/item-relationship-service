//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.component.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.stream.Stream;

/**
 * AspectType information for a part tree
 */
@SuppressWarnings("PMD.ShortMethodName")
public enum AspectType {
    SERIAL_PART_TYPIZATION(AspectTypesConstants.SERIAL_PART_TYPIZATION),
    ASSEMBLY_PART_RELATIONSHIP(AspectTypesConstants.ASSEMBLY_PART_RELATIONSHIP),
    PART_DIMENSION(AspectTypesConstants.PART_DIMENSION),
    SUPPLY_RELATION_DATA(AspectTypesConstants.SUPPLY_RELATION_DATA),
    PCF_CORE_DATA(AspectTypesConstants.PCFCORE_DATA),
    PCF_TECHNICAL_DATA(AspectTypesConstants.PCFTECHNICAL_DATA),
    MARKET_PLACE_OFFER(AspectTypesConstants.MARKET_PLACE_OFFER1),
    MATERIAL_ASPECT(AspectTypesConstants.MATERIAL_ASPECT1),
    BATTERY_PASS(AspectTypesConstants.BATTERY_PASS1),
    PRODUCT_DESCRIPTION_VEHICLE(AspectTypesConstants.PRODUCT_DESCRIPTION_VEHICLE1),
    PRODUCT_DESCRIPTION_BATTERY(AspectTypesConstants.PRODUCT_DESCRIPTION_BATTERY1),
    RETURN_REQUEST(AspectTypesConstants.RETURN_REQUEST1),
    CERTIFICATION_OF_DESTRUCTION(AspectTypesConstants.CERTIFICATE_OF_DESTRUCTION),
    CERTIFICATE_OF_DISMANTLER(AspectTypesConstants.CERTIFICATE_OF_DISMANTLER1),
    ADDRESS(AspectTypesConstants.ADDRESS1),
    CONTACT(AspectTypesConstants.CONTACT1);

    private final String value;

    AspectType(final String value) {
        this.value = value;
    }

    /**
     * of as a substitute/alias for valueOf handling the default value
     *
     * @param value see {@link #value}
     * @return the corresponding AspectType
     */
    public static AspectType value(final String value) {
        return AspectType.valueOf(value);
    }

    @JsonCreator
    public static AspectType fromValue(final String value) {
        return Stream.of(AspectType.values()).filter(aspectType -> aspectType.value.equals(value)).findFirst().orElseThrow();
    }

    /**
     * @return convert AspectType to string value
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * Constants for aspectTypes
     */
    public static class AspectTypesConstants {
        public static final String SERIAL_PART_TYPIZATION = "SerialPartTypization";
        public static final String ASSEMBLY_PART_RELATIONSHIP = "AssemblyPartRelationship";
        public static final String SUPPLY_RELATION_DATA = "SupplyRelationData";
        public static final String PART_DIMENSION = "PartDimension";
        public static final String PCFCORE_DATA = "PCFCoreData";
        public static final String PCFTECHNICAL_DATA = "PCFTechnicalData";
        public static final String MARKET_PLACE_OFFER1 = "MarketPlaceOffer";
        public static final String MATERIAL_ASPECT1 = "MaterialAspect";
        public static final String BATTERY_PASS1 = "BatteryPass";
        public static final String PRODUCT_DESCRIPTION_VEHICLE1 = "ProductDescriptionVehicle";
        public static final String PRODUCT_DESCRIPTION_BATTERY1 = "ProductDescriptionBattery";
        public static final String RETURN_REQUEST1 = "ReturnRequest";
        public static final String CERTIFICATE_OF_DESTRUCTION = "CertificateOfDestruction";
        public static final String CERTIFICATE_OF_DISMANTLER1 = "CertificateOfDismantler";
        public static final String ADDRESS1 = "Address";
        public static final String CONTACT1 = "Contact";
    }
}
