package net.catenax.irs.aaswrapper.job.delegate;

import static org.mockito.Mockito.mock;

import net.catenax.irs.aaswrapper.submodel.domain.SubmodelFacade;
import net.catenax.irs.semanticshub.SemanticsHubFacade;
import net.catenax.irs.services.validation.JsonValidatorService;
import net.catenax.irs.util.JsonUtil;

class SubmodelDelegateTest {

    final SubmodelFacade submodelFacade = mock(SubmodelFacade.class);
    final SemanticsHubFacade semanticsHubFacade = mock(SemanticsHubFacade.class);
    final JsonValidatorService jsonValidatorService = mock(JsonValidatorService.class);
    final SubmodelDelegate submodelDelegate = new SubmodelDelegate(null, submodelFacade,
            semanticsHubFacade, jsonValidatorService, new JsonUtil());



}
