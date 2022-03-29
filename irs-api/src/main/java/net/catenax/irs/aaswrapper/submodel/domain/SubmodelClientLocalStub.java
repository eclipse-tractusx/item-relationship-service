package net.catenax.irs.aaswrapper.submodel.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.aspectmodels.AspectModel;
import net.catenax.irs.aspectmodels.AspectModelTypes;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;
import net.catenax.irs.aspectmodels.assemblypartrelationship.ChildData;
import net.catenax.irs.aspectmodels.assemblypartrelationship.LifecycleContextCharacteristic;
import net.catenax.irs.aspectmodels.assemblypartrelationship.MeasurementUnit;
import net.catenax.irs.aspectmodels.assemblypartrelationship.Quantity;
import net.catenax.irs.aspectmodels.serialparttypization.ClassificationCharacteristic;
import net.catenax.irs.aspectmodels.serialparttypization.KeyValueList;
import net.catenax.irs.aspectmodels.serialparttypization.ManufacturingEntity;
import net.catenax.irs.aspectmodels.serialparttypization.PartTypeInformationEntity;
import net.catenax.irs.aspectmodels.serialparttypization.SerialPartTypization;
import org.springframework.stereotype.Service;

/**
 * Digital Twin Registry Rest Client Stub used in local environment
 */
//@Profile("local")
@Service
@ExcludeFromCodeCoverageGeneratedReport
public class SubmodelClientLocalStub implements SubmodelClient {

    @Override
    public AspectModel getSubmodel(final String endpointPath, final AspectModelTypes aspectModel) {
        switch (aspectModel) {
        case SERIAL_PART_TYPIZATION:
            return new SerialPartTypization("catenaXIdSerialPartTypization", Set.of(new KeyValueList("key", "value")),
                    new ManufacturingEntity(null, Optional.of("de")),
                    new PartTypeInformationEntity("manufacturerPartId", Optional.of("customerPartId"),
                            "nameAtManufacturer", Optional.of("nameAtCustomer"), ClassificationCharacteristic.PRODUCT));
        case ASSEMBLY_PART_RELATIONSHIP:

            final String now = Instant.now().toString();
            XMLGregorianCalendar date = null;
            try {
                date = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            } catch (DatatypeConfigurationException e) {
                e.printStackTrace();
            }
            return new AssemblyPartRelationship("catenaXIdAssemblyPartRelationship",
                    Set.of(new ChildData(date, new Quantity(1.0, new MeasurementUnit("test", "l")), null,
                            LifecycleContextCharacteristic.ASBUILT, null, "childCatenaXId")));
        default:
            return null;
        }

    }
}
