package net.catenax.irs.component.enums;

/**
 * Aspect information for a part tree
 */
public enum Aspect {
   SERIAL_PART_TYPIZATION("SerialPartTypization"),
   PART_DIMENSION("PartDimension"),
   SUPPLY_RELATION_DATA("SupplyRelationData"),
   PCF_CORE_DATA("PCFCoreData"),
   PCF_TECHNICAL_DATA("PCFTechnicalData"),
   MARKET_PLACE_OFFER("MarketPlaceOffer"),
   MATERIAL_ASPECT("MaterialAspect"),
   BATTERY_PASS("BatteryPass"),
   PRODUCT_DESCRIPTION_VEHICLE("ProductDescription_Vehicle"),
   PRODUCT_DESCRIPTION_BATTERY("ProductDescription_Battery"),
   RETURN_REQUEST("ReturnRequest"),
   CERTIFICATION_OF_DESTRUCTION("CertificateOfDestruction"),
   CERTIFICATE_OF_DISMANTLER("CertificateOfDismantler"),
   ADDRESS("Address"),
   CONTACT("Contact");

   private String value;

   Aspect(String value) {
      this.value = value;
   }

   /**
    * Default constant value
    */
   private final static Aspect defaultValue = SERIAL_PART_TYPIZATION;

   /**
    * of as a substitute/alias for valueOf handling the default value
    * @param value see {@link #value}
    * @return
    */
   public static Aspect of(String value) {
      return Aspect.valueOf(value);
   }

   /**
    *
    * @return
    */
   @Override
   public String toString() {
      return value;
   }

}
