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
    VEHICLE_DIAGNOSTIC_DATA_QUALITY(AspectTypesConstants.VEHICLE_DIAGNOSTIC_DATA_QUALITY);

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
        return Stream.of(AspectType.values())
                     .filter(aspectType -> aspectType.value.equals(value))
                     .findFirst()
                     .orElseThrow(() -> new NoSuchElementException("Unsupported AspectType: " + value
                             + ". Must be one of: " + supportedAspectTypes()));
    }

    private static String supportedAspectTypes() {
        return Stream.of(AspectType.values()).map(aspect -> aspect.value).collect(Collectors.joining(", "));
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
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class AspectTypesConstants {
        public static final String SERIAL_PART_TYPIZATION = "SerialPartTypization";
        public static final String ASSEMBLY_PART_RELATIONSHIP = "AssemblyPartRelationship";
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
    }
}
