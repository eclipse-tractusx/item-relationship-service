package net.catenax.irs.aspectmodels.serialparttypization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

class SerialPartTypizationTest {
    @Test
    void createSerialPartTypizationTest() {
        final PartTypeInformationEntity partTypeInformation = new PartTypeInformationEntity("manufacturerPartId",
                Optional.empty(), "nameAtManufacturer", Optional.empty(), ClassificationCharacteristic.PRODUCT);

        final String catenaXId = "catenaXId";
        final KeyValueList localIdentifier = new KeyValueList("testKey", "testValue");
        final ManufacturingEntity manufacturingInformation = new ManufacturingEntity(null, Optional.empty());
        final SerialPartTypization serialPartTypization = new SerialPartTypization(catenaXId, Set.of(localIdentifier),
                manufacturingInformation, partTypeInformation);

        assertThat(serialPartTypization.getPartTypeInformation()).isEqualTo(partTypeInformation);
        assertThat(serialPartTypization.getCatenaXId()).isEqualTo(catenaXId);
        assertThat(serialPartTypization.getLocalIdentifiers()).contains(localIdentifier);
        assertThat(serialPartTypization.getManufacturingInformation()).isEqualTo(manufacturingInformation);

        assertThat(serialPartTypization.getPartTypeInformation().getCustomerPartId()).isNotPresent();
        assertThat(serialPartTypization.getPartTypeInformation().getNameAtCustomer()).isNotPresent();
        assertThat(serialPartTypization.getPartTypeInformation().getManufacturerPartId()).isEqualTo(
                "manufacturerPartId");
        assertThat(serialPartTypization.getPartTypeInformation().getNameAtManufacturer()).isEqualTo(
                "nameAtManufacturer");
        assertThat(serialPartTypization.getPartTypeInformation().getClassification()).isEqualTo(
                ClassificationCharacteristic.PRODUCT);

        assertThat(serialPartTypization.getManufacturingInformation().getCountry()).isNotPresent();
        assertThat(serialPartTypization.getManufacturingInformation().getDate()).isNull();
    }
}