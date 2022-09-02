package net.catenax.irs.aaswrapper.job.delegate;

import static net.catenax.irs.util.TestMother.jobParameterFilter;
import static net.catenax.irs.util.TestMother.shellDescriptor;
import static net.catenax.irs.util.TestMother.submodelDescriptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.aaswrapper.job.ItemContainer;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelFacade;
import net.catenax.irs.semanticshub.SemanticsHubFacade;
import net.catenax.irs.services.validation.JsonValidatorService;
import net.catenax.irs.util.JsonUtil;
import org.junit.jupiter.api.Test;

class SubmodelDelegateTest {

    final SubmodelFacade submodelFacade = mock(SubmodelFacade.class);
    final SemanticsHubFacade semanticsHubFacade = mock(SemanticsHubFacade.class);
    final JsonValidatorService jsonValidatorService = mock(JsonValidatorService.class);
    final SubmodelDelegate submodelDelegate = new SubmodelDelegate(null, submodelFacade,
            semanticsHubFacade, jsonValidatorService, new JsonUtil());

    @Test
    void shouldFilterSubmodelDescriptorsByAspectTypeFilter() {
        // given
        final ItemContainer.ItemContainerBuilder itemContainerShellWithTwoSubmodels = ItemContainer.builder().shell(shellDescriptor(
                List.of(submodelDescriptor("urn:bamm:com.catenax.serial_part_typization:1.0.0#SerialPartTypization",
                                "testSerialPartTypizationEndpoint"),
                        submodelDescriptor("urn:bamm:com.catenax.assembly_part_relationship:1.0.0#AssemblyPartRelationship",
                                "testAssemblyPartRelationshipEndpoint"))));

        // when
        final ItemContainer result = submodelDelegate.process(itemContainerShellWithTwoSubmodels, jobParameterFilter(),
                new AASTransferProcess(), "itemId");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getShells().get(0).getSubmodelDescriptors().size()).isZero();
    }

}
