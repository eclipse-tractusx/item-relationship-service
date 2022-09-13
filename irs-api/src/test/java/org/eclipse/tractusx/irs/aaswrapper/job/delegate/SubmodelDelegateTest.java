package org.eclipse.tractusx.irs.aaswrapper.job.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.util.TestMother.jobParameterFilter;
import static org.eclipse.tractusx.irs.util.TestMother.shellDescriptor;
import static org.eclipse.tractusx.irs.util.TestMother.submodelDescriptor;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.aaswrapper.submodel.domain.SubmodelFacade;
import org.eclipse.tractusx.irs.semanticshub.SemanticsHubFacade;
import org.eclipse.tractusx.irs.services.validation.JsonValidatorService;
import org.eclipse.tractusx.irs.util.JsonUtil;
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
        assertThat(result.getShells().get(0).getSubmodelDescriptors()).isEmpty();
    }

}
